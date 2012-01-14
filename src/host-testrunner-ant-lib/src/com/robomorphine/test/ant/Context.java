package com.robomorphine.test.ant;

import com.robomorphine.test.TestManager;

public class Context {
    
    private TestManager mTestManager;
    private String mSerialNo;
    private boolean mVerbose = false;
    
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
    
    public void setVerbose(boolean verbose) {
        mVerbose = verbose;
    }
    
    public boolean isVerbose() {
        return mVerbose;
    }
}
