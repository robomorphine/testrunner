package com.robomorphine.test;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.ISdkLog;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.avd.AvdManager;
import com.robomorphine.test.exception.AdbConnectionException;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.ISdkLog2ILog;
import com.robomorphine.test.log.PrefixedLog;
import com.robomorphine.test.sdktool.AdbTool;
import com.robomorphine.test.sdktool.ToolsManager;

import java.io.File;

public class TestManager {
    
    private final static long ADB_CONNECTION_CHECK_INTERVAL = 500;
    
    private final File mSdkPath;
    private final ILog mOriginalLog;
    private final ILog mLog;
    private final ISdkLog mSdkLog;
    
    private final SdkManager mSdkManager;
    private AndroidDebugBridge mAdb;
    
    private final ToolsManager mToolsManager;
    private final AvdManager mAvdManager;
    private final ApkManager mApkManager;
    
    public TestManager(File sdkPath, ILog log) throws AndroidLocationException {
        mSdkPath = sdkPath;
        mOriginalLog = log;
        mLog = new PrefixedLog(TestManager.class.getSimpleName(), log);
        mSdkLog = new ISdkLog2ILog(new PrefixedLog("SDK", mOriginalLog));
        
        mSdkManager = SdkManager.createManager(sdkPath.getAbsolutePath(), mSdkLog);
        
        mToolsManager = new ToolsManager(sdkPath, mOriginalLog);
        mAvdManager = new AvdManager(mSdkManager, mSdkLog);
        mApkManager = new ApkManager(this);
    }
    
    public ILog getLogger() {
        return mOriginalLog;
    }
    
    public ILog newPrefixedLogger(Class<?> clazz) {
        return new PrefixedLog(clazz.getSimpleName(), getLogger()); 
    }
    
    public ISdkLog getSdkLogger() {
        return mSdkLog;
    }    
    
    public SdkManager getSdkManager() {
        return mSdkManager;
    }
    
    public AndroidDebugBridge getAndroidDebugBridge() {
        if(mAdb == null) {
            throw new IllegalStateException("ADB is not connected.");
        }
        return mAdb;
    }
    
    public ToolsManager getToolsManager() {
        return mToolsManager;
    }
    
    public AvdManager getAvdManager() {
        return mAvdManager;
    }
    
    public ApkManager getApkManager() {
        return mApkManager;
    }
    
    public boolean isAdbConnected() {
        return mAdb != null && mAdb.isConnected();
    }
    
    public void reconnectAdb() throws AdbConnectionException {
        mAdb.restart();
        connectAdb();
    }
    
    public void connectAdb()  throws AdbConnectionException {
        try {
            AndroidDebugBridge.init(false);
            mLog.i("ADB initialized.");
        } catch(IllegalStateException ex) {
            //ignore, thrown if ADB was already initialized            
        }
        File path = AdbTool.getAdbPath(mSdkPath);
        mAdb = AndroidDebugBridge.createBridge(path.getAbsolutePath(), false);        
        if(mAdb == null) {
            throw new AdbConnectionException("Failed to start adb connection");
        }
        mLog.i("ADB created.");
        
        while(!mAdb.hasInitialDeviceList()) {
            try {
                Thread.sleep(ADB_CONNECTION_CHECK_INTERVAL);
                mLog.v("ADB connecting...");
            } catch(InterruptedException ex){
                //ignore
            }
        }
        mLog.i("ADB connected.");
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
