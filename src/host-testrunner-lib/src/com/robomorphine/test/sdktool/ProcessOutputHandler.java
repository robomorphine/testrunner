package com.robomorphine.test.sdktool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

class ProcessOutputHandler implements Runnable {
    
    public interface HandlerListener {
        void onLine(String line);
        void onCompleted(Throwable ex);
    }
    
    private final HandlerListener mListener;
    private final InputStream mProcessIn;
    private final OutputStream mOutput;
    
    public ProcessOutputHandler(InputStream processIn, OutputStream out, HandlerListener listener) {
        if(processIn == null || listener == null) {
            throw new NullPointerException();
        }
        mProcessIn = processIn;
        mOutput = out;
        mListener = listener;
    }
    
    @Override
    public void run() {
        handle();
    }
    
    public void handle() {
        Throwable exception = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(mProcessIn));
                
        OutputStreamWriter writer = null;
        if(mOutput != null) {
            writer = new OutputStreamWriter(mOutput);
        }
        
        try {
            String line = null;
            while((line = reader.readLine()) != null) {
                if(Thread.interrupted()) {
                    break;
                }
                mListener.onLine(line);
                if(writer != null) {
                    writer.write(line);    
                    writer.write("\n");
                }
            }
            if(writer != null) {
                writer.flush();
                writer.close();
            }
        } catch(IOException ex) {
            
        } finally {
            mListener.onCompleted(exception);
        }
    }
}
