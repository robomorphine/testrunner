package com.robomorphine.test;

public class AdbConnectionException  extends Exception {
    static final long serialVersionUID = 1L;
    
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
