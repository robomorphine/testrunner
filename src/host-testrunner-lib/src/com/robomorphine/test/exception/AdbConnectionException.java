package com.robomorphine.test.exception;

public class AdbConnectionException  extends Exception {
    private static final long serialVersionUID = 1L;
    
    public AdbConnectionException(Throwable ex, String msg) {
        super(msg, ex);
    }
    
    public AdbConnectionException(Throwable ex) {
        super(ex);
    }
    
    public AdbConnectionException(String msg) {
        super(msg);
    }
}
