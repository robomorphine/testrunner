package com.robomorphine.test.sdktool;


public class ProcessCompletionWaiter implements Runnable {
    
    public interface CompletionListener {
        void onCompleted(int exitValue);
    }
    
    private final CompletionListener mListener;
    private final Process mProcess;
    private boolean mCompleted;
    
    public ProcessCompletionWaiter(Process process, CompletionListener listener) {
        mProcess = process;
        mListener = listener;
    }
    
    public boolean isCompleted() {
        synchronized(this) {
            return mCompleted;
        }
    }
    
    public void waitForCompletion() throws InterruptedException {
        synchronized (this) {
            while(!isCompleted()) {
                wait();
            }
        }
    }
    
    @Override
    public void run() {
        int exitValue = -1; 
        try {
            mProcess.waitFor(); 
        } catch(InterruptedException ex) {
            //ignore
        }
                
        try {
            exitValue = mProcess.exitValue();
        } catch(IllegalThreadStateException ex) {
            //ignore
        }
        
        synchronized(this) {
            mCompleted = true;
            notifyAll();
        }   
        mListener.onCompleted(exitValue);
    }
}
