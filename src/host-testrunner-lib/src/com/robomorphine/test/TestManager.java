package com.robomorphine.test;

import com.android.sdklib.SdkManager;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.ISdkLog2ILog;

public class TestManager {
    
    private final SdkManager mSdkManager;
    
    public TestManager(String path, ILog log) {
        mSdkManager = SdkManager.createManager(path, new ISdkLog2ILog(log));
    }
    
    
    public void connectAdb() {
    }
    
    public void disconnectAdb() {
    }
    
    public void lockDevice() {
    }
    
    public void installApk(){
    }
    
    public void uninstallApk(){
    }
    
}
