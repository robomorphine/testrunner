package com.robomorphine.test.exception;

public class DeviceNotConnectedException extends Exception {
    
    static final long serialVersionUID = 1L;
    
    public DeviceNotConnectedException() {
    }
        
    public DeviceNotConnectedException(Throwable ex) {
        super(ex);
    }
    
    public DeviceNotConnectedException(String msg) {
        super(msg);
    }
    
    public DeviceNotConnectedException(Throwable ex, String msg) {
        super(msg, ex);
    }

}
