package com.robomorphine.test;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.ISdkLog;
import com.android.sdklib.SdkManager;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.ISdkLog2ILog;
import com.robomorphine.test.sdktool.AdbTool;
import com.robomorphine.test.sdktool.ToolsManager;

import java.io.File;

public class TestManager {
    
    private final File mSdkPath;
    private final ILog mLog;
    private final ISdkLog mSdkLog;
    
    private final SdkManager mSdkManager;
    private AndroidDebugBridge mAdb;
    
    private final ToolsManager mToolsManager;
    private final AvdManager mAvdManager;    
    
    public TestManager(File sdkPath, ILog log) throws AndroidLocationException {
        mSdkPath = sdkPath;
        mLog = log;
        mSdkLog = new ISdkLog2ILog(log);
        
        mSdkManager = SdkManager.createManager(sdkPath.getAbsolutePath(), mSdkLog);
        
        mToolsManager = new ToolsManager(sdkPath, mLog);
        mAvdManager = new AvdManager(mSdkManager, mSdkLog);
    }
    
    public ILog getLogger() {
        return mLog;
    }
    
    public ISdkLog getSdkLogger() {
        return mSdkLog;
    }    
    
    public SdkManager getSdkManager() {
        return mSdkManager;
    }
    
    public AndroidDebugBridge getAndroidDebugBridge() {
        return mAdb;
    }
    
    public ToolsManager getToolsManager() {
        return mToolsManager;
    }
    
    public AvdManager getAvdManager() {
        return mAvdManager;
    }
    
    public boolean isAdbConnected() {
        return mAdb != null && mAdb.isConnected();
    }
    
    public void connectAdb() {
        try {
            AndroidDebugBridge.init(false);
            mLog.info("ADB initialized.");
        } catch(IllegalStateException ex) {
            //ignore, thrown if ADB was already initialized            
        }
        File path = AdbTool.getAdbPath(mSdkPath);
        mAdb = AndroidDebugBridge.createBridge(path.getAbsolutePath(), false);        
        if(mAdb == null) {
            throw new IllegalStateException("Failed to start adb connection");
        }
        mLog.info("ADB created.");
        
        while(!mAdb.hasInitialDeviceList()) {
            try {
                Thread.sleep(250);
                mLog.info("ADB connecting...");
            } catch(InterruptedException ex){
                //ignore
            }
        }
        mLog.info("ADB connected.");
    }
    
    public void disconnectAdb() {
        AndroidDebugBridge.disconnectBridge();
    }
    
    public void lockDevice() {
    }
    
    public void installApk(){
    }
    
    public void uninstallApk(){
    }
    
}
