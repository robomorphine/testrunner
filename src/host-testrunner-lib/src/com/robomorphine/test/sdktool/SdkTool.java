package com.robomorphine.test.sdktool;

import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.PrefixedLog;
import com.robomorphine.test.sdktool.ProcessCompletionWaiter.CompletionListener;
import com.robomorphine.test.sdktool.ProcessOutputHandler.HandlerListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SdkTool {
    
    private enum State { IDLE, RUNNING, COMPLETED };
    
    public static class Result {
        private final int mExitCode;
        private final String mStdOut;
        private final String mStdErr;
        
        Result(int exitCode, String out, String err) {
            mExitCode = exitCode;
            mStdOut = out;
            mStdErr = err;
        }
        
        public int getExitCode() {
            return mExitCode;
        }
        
        public String getStdOut() {
            return mStdOut;
        }
        
        public String getStdErr() {
            return mStdErr;
        }
    }
    
    public interface ToolListener {
        void onStarted();
        void onCompleted(int exitCode);
        void onStdOutput(String outLine);
        void onStdError(String errLine);
    }
    
    private HandlerListener mStdOutHandlerListener = new HandlerListener() {
        @Override
        public void onLine(String line) {
            if(mToolListener != null) {
                mToolListener.onStdOutput(line);
            }
        }
        
        @Override
        public void onCompleted(Throwable ex) {
        };
    };
    
    private HandlerListener mStdErrHandlerListener = new HandlerListener() {
        
        @Override        
        public void onLine(String line) {
            if(mToolListener != null) {
                mToolListener.onStdOutput(line);
            }
        }
        
        @Override
        public void onCompleted(Throwable ex) {
        }
    };
    
    private CompletionListener mCompletionListener = new CompletionListener() {
        @Override
        public void onCompleted(int exitValue) {
            if(mToolListener != null) {
                mToolListener.onCompleted(exitValue);
            }
            if(mManager != null) {
                mManager.onProcessCompleted(mProcess);
            }
            mLog.info("Tool %s exit code: %d", SdkTool.this.getClass().getSimpleName(), exitValue);
            mState = State.COMPLETED;
        }
    };
        
    private final ToolsManager mManager;
    private final ILog mLog;
    private final File mExePath;
    private final List<String> mArguments = new LinkedList<String>();
    private final HashMap<String, String> mEnv = new HashMap<String, String>();
    
    private State mState = State.IDLE;
    private Process mProcess;
    private ToolListener mToolListener;
    private boolean mStoreOutput = true;
    
    private ProcessCompletionWaiter mProcessCompletionWaiter;
    private Thread mProcessCompletionWaiterThread;
    
    private ByteArrayOutputStream mStdOut;
    private ProcessOutputHandler mStdOutHandler;
    private Thread mStdOutHandlerThread;
    
    private ByteArrayOutputStream mStdErr;
    private ProcessOutputHandler mStdErrHandler;
    private Thread mStdErrHandlerThread;
    
    SdkTool(File exePath, ToolsManager manager) {
        mExePath = exePath;        
        mManager = manager;
        mLog = new PrefixedLog(getClass().getSimpleName(), manager.getLogger());
    }
    
    private void assertState(State...states) {
        for(State state : states) {
            if(mState == state) {
                return;
            }
        }
        throw new IllegalStateException("Operation not permitted in this state: " + mState);
    }
    
    public void addArgument(String arg) {
        assertState(State.IDLE);
        mArguments.add(arg);
    }
    
    public void addArgument(String...args) {
        assertState(State.IDLE);
        for(String arg : args) {         
            addArgument(arg);
        }
    }
    
    public void addArgument(List<String> args) {
        assertState(State.IDLE);
        for(String arg : args) {         
            addArgument(arg);
        }
    }
    
    public void setEnv(String name, String value) {
        assertState(State.IDLE);
        mEnv.put(name, value);
    }
    
    public void setListner(ToolListener listener) {
        assertState(State.IDLE);
        mToolListener = listener;
    }
    
    public void setStoreOutput(boolean store) {
        assertState(State.IDLE);
        mStoreOutput = store;
    }
    
    public Result execute() throws IOException, InterruptedException {
        assertState(State.IDLE);
        startExecution();
        waitForCompletion();
        return getResult();
    }
    
    public void startExecution() throws IOException {
        assertState(State.IDLE);
        ProcessBuilder builder = new ProcessBuilder(mArguments);
        
        /* args */
        List<String> args = new LinkedList<String>();
        args.add(mExePath.getAbsolutePath());
        args.addAll(mArguments);
        builder.command(args);
        
        StringBuilder args4Log = new StringBuilder();
        for(String arg : args) {
            args4Log.append(arg);
            args4Log.append(" ");
        }
        mLog.info("Executing %s tool: %s", getClass().getSimpleName(), args4Log.toString());
                    
        
        /* env */
        builder.environment().putAll(mEnv);
        
        mProcess = builder.start();
        mState = State.RUNNING;
        if(mManager != null) {
            mManager.onProcessStarted(mProcess);
        }
        if(mToolListener != null) {
            mToolListener.onStarted();
        }
        
        mProcessCompletionWaiter = new ProcessCompletionWaiter(mProcess, mCompletionListener);
        mProcessCompletionWaiterThread = new Thread(mProcessCompletionWaiter);
        mProcessCompletionWaiterThread.start();
                
        if(mStoreOutput) {
            mStdOut = new ByteArrayOutputStream();
            mStdErr = new ByteArrayOutputStream();
        }
        
        mStdErrHandler = new ProcessOutputHandler(mProcess.getErrorStream(), mStdErr, mStdErrHandlerListener);
        mStdErrHandlerThread = new Thread(mStdErrHandler);
        mStdErrHandlerThread.start();
        
        mStdOutHandler = new ProcessOutputHandler(mProcess.getInputStream(), mStdOut, mStdOutHandlerListener);
        mStdOutHandlerThread = new Thread(mStdOutHandler);
        mStdOutHandlerThread.start();        
    }
    
    public void waitForCompletion() throws InterruptedException {
        assertState(State.RUNNING, State.COMPLETED);
        mProcessCompletionWaiter.waitForCompletion();
        mStdErrHandlerThread.join();
        mStdOutHandlerThread.join();
    }
    
    public Result getResult() {
        if(!mProcessCompletionWaiter.isCompleted()) {
            return null;
        }        
        int exitCode = mProcess.exitValue();
        String stdOut = null;
        String stdErr = null;
        if(mStdOut != null) {
            byte [] data = mStdOut.toByteArray();
            stdOut = new String(data, 0, data.length, Charset.defaultCharset());
        }
        if(mStdErr != null) {
            byte [] data = mStdErr.toByteArray();
            stdErr = new String(data, 0, data.length, Charset.defaultCharset());
        }
        
        Result result = new Result(exitCode, stdOut, stdErr);
        return result;
    }
    
    public void terminate() {
        mState = State.COMPLETED;
        if(mProcess != null) {
            mProcess.destroy();
        }
        if(mProcessCompletionWaiterThread != null) {
            mProcessCompletionWaiterThread.interrupt();
        }
        if(mStdOutHandlerThread != null) {
            mStdOutHandlerThread.interrupt();
        }
        if(mStdErrHandlerThread != null) {
            mStdErrHandlerThread.interrupt();
        }
    }
}
