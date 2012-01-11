package com.robomorphine.test.exception;

public class DeviceNotConnectedException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public DeviceNotConnectedException() {
        super();
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
