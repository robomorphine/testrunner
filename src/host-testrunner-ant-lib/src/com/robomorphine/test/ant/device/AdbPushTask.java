package com.robomorphine.test.ant.device;

import com.robomorphine.test.ant.BaseTask;

import org.apache.tools.ant.BuildException;

public class AdbPushTask extends BaseTask {
    private String mLocalFile;
    private String mRemoteFile;
    
    public void setLocal(String file) {
        mLocalFile = file;
    }
    
    public void setRemote(String file) {
        mRemoteFile = file;
    }
    
    @Override
    public void execute() throws BuildException {
        if(mLocalFile == null) {
            error("Host path is not specified.");
        }
        
        if(mRemoteFile == null) {
            error("Device path is not specified.");
        }
        
        try {
            getDevice().pushFile(mLocalFile, mRemoteFile);
        } catch(Exception ex) {
            error("Failed to push file to device %s: %s -> %s", 
                  getDeviceSerialNumber(), mLocalFile, mRemoteFile);
        }
    }
}
