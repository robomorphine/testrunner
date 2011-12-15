package com.robomorphine.test.ant;

import com.robomorphine.test.TestManager;

public class Context {
    
    private TestManager mTestManager;
    private String mSerialNo;
    
    public void setTestManager(TestManager manager) {
        mTestManager = manager;
    }
    
    public TestManager getTestManager() {
        return mTestManager;
    }
    
    public void setDeviceSerialNumber(String serialNo) {
        mSerialNo = serialNo;
    }
    
    public String getDeviceSerialNumber() {
        return mSerialNo;
    }
}
