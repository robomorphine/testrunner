package com.robomorphine.test.ant.device;

import com.robomorphine.test.ant.BaseTask;

import org.apache.tools.ant.BuildException;

public class AdbPullTask extends BaseTask {
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
            getDevice().pullFile(mRemoteFile, mLocalFile);
        } catch(Exception ex) {
            error("Failed to pull file from device %s: %s - > %s", 
                  getDeviceSerialNumber(), mRemoteFile, mLocalFile);
        }
    }
       
}
