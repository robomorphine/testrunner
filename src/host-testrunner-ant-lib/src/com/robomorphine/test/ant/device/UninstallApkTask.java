package com.robomorphine.test.ant.device;

import com.robomorphine.test.ApkManager;
import com.robomorphine.test.ant.BaseTask;

import org.apache.tools.ant.BuildException;

import java.io.File;


public class UninstallApkTask extends BaseTask {

    private String mPackageName;
    private File mApkFile;
    private int mAttempts = ApkManager.DEFAULT_ATTEMPTS_COUNT;
    
    public void setPackage(String packageName) {
        mPackageName = packageName;
    }
    
    public void setFile(File file) {
        mApkFile = file;
    }
    
    public void setAttempts(int attempts) {
        mAttempts = attempts;
    }
    
    public void execute() throws BuildException {
        if(mPackageName == null && mApkFile == null) {
            error("Apk file or package name must be specified. Use \"file\" or \"package\" attributes.");
        }
        
        if(mPackageName != null && mApkFile != null) {
            warn("Both apk file and package name are speicifed! Apk file is ignored.");
        }
        
        
        ApkManager apkManager = getTestManager().getApkManager();
        if(mPackageName != null) {
            try {
                apkManager.uninstall(getDevice(), mPackageName, mAttempts);
            } catch(Exception ex) {
                error(ex, "Failed to uninstall using package name: %s", mPackageName);
            }
        } else {
            try {
                apkManager.uninstall(getDevice(), mApkFile, mAttempts);
            } catch(Exception ex) {
                error(ex, "Failed to uninstall using apk file and extracted package name: %s",
                           mApkFile.getAbsolutePath());
            }
        }
    };
}
