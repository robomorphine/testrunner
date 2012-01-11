package com.robomorphine.test.ant.device;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.robomorphine.test.ant.BaseTask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.util.LinkedList;
import java.util.List;

public class AdbShellTask extends BaseTask { //NOPMD
        
    private static class StringReceiver extends Task implements IShellOutputReceiver {
        
        private final StringBuilder mStream = new StringBuilder();        
        private final boolean mLog;
        
        public StringReceiver(Project project, boolean log) {
            super();
            setTaskName("rbm-adb-shell-output");
            mLog = log;
            setProject(project);
        }
        
        @Override
        public boolean isCancelled() {
            return false;
        }
        
        @Override
        public void flush() {
            //ignored
        }
        
        @Override
        public void addOutput(byte[] buffer, int offset, int len) {
            String string = new String(buffer, offset, len);
            if(mLog) {
                log(string);
            }
            mStream.append(string);
        }
        
        public String getOutput() {
            return mStream.toString();
        }
    }
    
    public static class Arg {
        private String mArg;
        
        public void setValue(String arg) {
            mArg = arg;
        }
        
        public String getValue() {
            return mArg; 
        }
    }
    
    private String mCmd;
    private final List<String> mArgs = new LinkedList<String>();
    private String mOutputProperty;
    private boolean mLog = true;
    
    private String mExitCodeProperty;
    private int mExpectedExitCode = 0;
    private boolean mExitCodeFail = true;
    
        
    public void setCmd(String cmd) {
        mCmd = cmd;
    }
    
    public void setLog(boolean log) {
        mLog = log;
    }
    
    public void setOutputProperty(String name) {
        mOutputProperty = name;
    }
    
    public void addConfiguredArg(Arg arg) {
        if(arg.getValue() != null) {
            mArgs.add(arg.getValue());
        }
    }
    
    public void setExitCodeProperty(String name) {
        mExitCodeProperty = name;
    }
    
    public void setExitCodeExpected(int code) {
        mExpectedExitCode = code;
    }
    
    public void setExitCodeFail(boolean fail) {
        mExitCodeFail = fail;
    }
    
    @Override
    public void execute() throws BuildException { //NOPMD
        
        StringBuilder cmd = new StringBuilder();
        if(mCmd != null) {
            mArgs.add(0, mCmd);
        }
        for(String arg : mArgs) {
            if(cmd.length() > 0) {
                cmd.append(" ");
            }
            cmd.append(arg);
        }
        
        StringReceiver receiver = new StringReceiver(getProject(), mLog);
        String output = "";
        try {
            info("executing: %s", cmd);
            String actualCmd = String.format("(%s); echo $?", cmd);
             
            IDevice device = getDevice();
            dbg("executing: %s", actualCmd);
            device.executeShellCommand(actualCmd, receiver);
            dbg("output: %s", receiver.getOutput());
            
            /* last line of output is exit code */
            String rawOutput = receiver.getOutput();
            String [] rawOutputLines = rawOutput.split("\n");
            String rawOutputLastLine = rawOutputLines[rawOutputLines.length - 1];
                        
            String exitCodeValue = rawOutputLastLine.trim();            
            info("exit code: %s", exitCodeValue);
                        
            int exitCode = 0;
            boolean lastLineIsErrorCode = true; 
            try {
                exitCode = Integer.parseInt(exitCodeValue); 
            } catch(NumberFormatException ex) {
                lastLineIsErrorCode = false;
                warn("Failed to parse \"%s\" as exit code.", exitCodeValue);
                if(mExitCodeFail) {
                    error(ex, "Invalid exit code: %s", exitCodeValue);
                }
            }
            
            if(lastLineIsErrorCode && mExitCodeProperty != null) {
                getProject().setProperty(mExitCodeProperty, exitCodeValue);
            }
            
            if(mExitCodeFail && mExpectedExitCode != exitCode) {
                error("Expected exit code: %d, actual exit code: %d", mExpectedExitCode, exitCode);
            }
            
            StringBuilder outputBuilder = new StringBuilder();
            for(int i = 0; i < rawOutputLines.length - 1; i++) {
                if(outputBuilder.length() > 0) {
                    outputBuilder.append("\n");
                }
                outputBuilder.append(rawOutputLines[i]);
            }
            if(!lastLineIsErrorCode) {
                outputBuilder.append("\n");
                outputBuilder.append(rawOutputLastLine);
            }
            output = outputBuilder.toString();
            
        } catch(Exception ex) {
            warn("%s", receiver.getOutput());
            error(ex, "Failed to execute adb shell command: %s", cmd);
        }
        
        if(mOutputProperty != null) {
            getProject().setProperty(mOutputProperty, output);
        }
        
    }
}
