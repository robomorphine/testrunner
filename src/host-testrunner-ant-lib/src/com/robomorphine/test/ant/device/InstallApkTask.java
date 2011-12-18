package com.robomorphine.test.ant.device;

import com.robomorphine.test.ApkManager;
import com.robomorphine.test.ant.BaseTask;

import org.apache.tools.ant.BuildException;

import java.io.File;

public class InstallApkTask extends BaseTask {

    private File mApkFile;
    private boolean mReinstall;
    public void setFile(File file) {
        mApkFile = file;
    }
    
    public void setReinstall(boolean reinstall) {
        mReinstall = reinstall;
    }
    
    @Override
    public void execute() throws BuildException {
        if(mApkFile == null) {
            error("Apk file was not specified. Use \"file\" attribute.");
        }
        
        if(!mApkFile.exists()) {
            error("Apk file does not exist: %s", mApkFile.getAbsolutePath());
        }
        
        ApkManager apkManager = getTestManager().getApkManager();
        try {
            apkManager.install(getDevice(), mApkFile, mReinstall);
        } catch(Exception ex) {
            error(ex, "Failed to install apk to device %s: %s", 
                       getDeviceSerialNumber(), mApkFile.getAbsolutePath());
        }
    }
}
