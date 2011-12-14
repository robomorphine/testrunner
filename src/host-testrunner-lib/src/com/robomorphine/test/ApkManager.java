package com.robomorphine.test;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.PrefixedLog;
import com.robomorphine.test.sdktool.AaptTool;

import java.io.File;
import java.io.IOException;

public class ApkManager {

    private final TestManager mTestManager;
    private final ILog mLog;
    
    public ApkManager(TestManager testManager) {
        mTestManager = testManager;
        mLog = new PrefixedLog(ApkManager.class.getSimpleName(), testManager.getLogger());
    }
    
    private IDevice findDevice(String serialNo) throws InstallException {
        AndroidDebugBridge adb = mTestManager.getAndroidDebugBridge();
        for(IDevice device : adb.getDevices()) {
            if(device.getSerialNumber().equals(serialNo)) {                
                return device;
            }
        }
        throw new InstallException("Device with serial number=\"" + serialNo + "\" not found.", 
                                    null);
    }
    
    public void install(String deviceSerialNo, File apkFile, boolean reinstall) throws IOException,
            InstallException {
        install(findDevice(deviceSerialNo), apkFile, reinstall);
    }
    
    public void install(IDevice device, File apkFile, boolean reinstall) throws IOException,
            InstallException {
        long time = System.currentTimeMillis();
        mLog.info("Installing %s to %s...", apkFile.getName(), device.getSerialNumber());
        device.installPackage(apkFile.getAbsolutePath(), reinstall);
        
        
        long delta = System.currentTimeMillis() - time;
        mLog.info("Installed %s to %s in %d seconds.", 
                  apkFile.getName(), device.getSerialNumber(), delta/1000);
    }
    
    public void uninstall(String deviceSerialNo, File apkFile) throws IOException, InstallException {
        uninstall(findDevice(deviceSerialNo), apkFile);
    }
    
    public void uninstall(IDevice device, File apkFile) throws IOException, InstallException {
        AaptTool aapt = mTestManager.getToolsManager().createAaptTool();
        mLog.info("Extracting package from %s...", apkFile.getName());
        String pkgName = aapt.getPacakgeName(apkFile);
        mLog.info("Package %s: %s", apkFile.getName(), pkgName);
        uninstall(device, pkgName);
    }
    
    public void uninstall(String deviceSerialNo, String pkgName) throws IOException, InstallException {
            uninstall(findDevice(deviceSerialNo), pkgName);
    }
    
    public void uninstall(IDevice device, String pkgName) throws IOException, InstallException {
        device.uninstallPackage(pkgName);
    }
    
  }
