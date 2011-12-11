package com.robomorphine.test.emulator;

public class EmulatorStarterException extends Exception {
    static final long serialVersionUID = 1L;
    
    public EmulatorStarterException(Throwable ex, String msg) {
        super(msg, ex);
    }
    
    public EmulatorStarterException(Throwable ex) {
        super(ex);
    }
    
    public EmulatorStarterException(String msg) {
        super(msg);
    }
}