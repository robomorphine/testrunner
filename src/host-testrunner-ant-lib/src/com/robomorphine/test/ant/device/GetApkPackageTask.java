package com.robomorphine.test.ant.device;

import com.robomorphine.test.ant.BaseTask;
import com.robomorphine.test.sdktool.AaptTool;

import org.apache.tools.ant.BuildException;

import java.io.File;

public class GetApkPackageTask extends BaseTask {

    private String mPackagePropertyName = null;
    private File mApkFile;
    
    public void setFile(File file) {
        mApkFile = file;
    }
    
    public void setProperty(String property) {
        mPackagePropertyName = property;
    }
    
    @Override
    public void execute() throws BuildException {
        if(mPackagePropertyName == null) {
            error("Name of property that should hold resulting package name is not specified! "+ 
                   "Use \"property\" attribute.");
        }
        
        if(mApkFile == null) {
            error("Apk file is not specified. Use \"file\" attribute.");
        }
        
        if(!mApkFile.exists()) {
            error("Apf file does not exist: %s", mApkFile.getAbsolutePath());
        }
        
        try {
            AaptTool aapt = getTestManager().getToolsManager().createAaptTool();
            info("Extracting package from %s...", mApkFile.getName());
            String pkgName = aapt.getPacakgeName(mApkFile);
            info("Package name is: %s", pkgName);
            getProject().setProperty(mPackagePropertyName, pkgName);            
        } catch(Exception ex) {
            error(ex, "Failed to extract package name from apk file: %s", mApkFile.getAbsolutePath());
        }
    }
}
