package com.robomorphine.test;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.sdktool.AaptTool;

import java.io.File;
import java.io.IOException;

/**
 * Handles installation and uninstallation of .apk to/from device or emulator
 */
public class ApkManager {
    
    public static final int DEFAULT_ATTEMPTS_COUNT = 3;

    private final TestManager mTestManager;
    private final ILog mLog;
    
    public ApkManager(TestManager testManager) {
        mTestManager = testManager;
        mLog = mTestManager.newPrefixedLogger(ApkManager.class);
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
        install(device, apkFile, reinstall, 3);
    }
    
    public void install(IDevice device, File apkFile, boolean reinstall, int attempts)
            throws IOException, InstallException {
        
        long time = System.currentTimeMillis();
        String path = apkFile.getAbsolutePath();
        mLog.i("Installing %s to %s...", apkFile.getName(), device.getSerialNumber());
        try {
            device.installPackage(path, reinstall);
        } catch(InstallException ex) {
            boolean unresponsive = ex.getCause() instanceof ShellCommandUnresponsiveException;
            if(unresponsive ){
                mLog.w("Failed to install apk due to unresponsive shell: %s", path);
                if(attempts > 1) {
                    mLog.w("Attempts left: %d", attempts);
                    install(device, apkFile, reinstall, attempts - 1);
                } else {
                    throw ex;
                }
            }
        }        
        long delta = System.currentTimeMillis() - time;
        mLog.i("Installed %s to %s in %.2f seconds.", 
                  apkFile.getName(), device.getSerialNumber(), delta/1000.0);
    }
    
    public void uninstall(String deviceSerialNo, File apkFile) throws IOException, InstallException {
        uninstall(findDevice(deviceSerialNo), apkFile);
    }
    
    public void uninstall(IDevice device, File apkFile) throws IOException, InstallException {
        uninstall(device, apkFile, DEFAULT_ATTEMPTS_COUNT);
    }
    
    public void uninstall(IDevice device, File apkFile, int attempts) throws IOException,
            InstallException {
        AaptTool aapt = mTestManager.getToolsManager().createAaptTool();
        mLog.v("Extracting package from %s...", apkFile.getName());
        String pkgName = aapt.getPacakgeName(apkFile);
        mLog.v("Package %s: %s", apkFile.getName(), pkgName, attempts);
        uninstall(device, pkgName);
    }
    
    public void uninstall(String deviceSerialNo, String pkgName) throws IOException,
            InstallException {
        uninstall(findDevice(deviceSerialNo), pkgName);
    }
    
    public void uninstall(IDevice device, String pkgName) throws IOException, InstallException {
        uninstall(device, pkgName, DEFAULT_ATTEMPTS_COUNT);
    }
    
    public void uninstall(IDevice device, String pkgName, int attempts) throws IOException,
            InstallException {
        
        mLog.i("Uninstalling %s...", pkgName);
        long time = System.currentTimeMillis();
        try {
            device.uninstallPackage(pkgName);
        } catch(InstallException ex) {
            boolean unresponsive = ex.getCause() instanceof ShellCommandUnresponsiveException;
            if(unresponsive ){
                mLog.w("Failed to uninstall apk due to unresponsive shell: %s", pkgName);
                if(attempts > 1) {
                    mLog.w("Attempts left: %d", attempts);
                    uninstall(device, pkgName, attempts - 1);
                } else {
                    throw ex;
                }
            }
        }
        long delta = System.currentTimeMillis() - time;
        mLog.i("Uninstalled %s in %.2f seconds.", pkgName, delta/1000.0);
    }
  }
