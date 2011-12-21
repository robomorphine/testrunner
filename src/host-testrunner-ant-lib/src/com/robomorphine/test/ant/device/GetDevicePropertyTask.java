package com.robomorphine.test.ant.device;

import com.robomorphine.test.ant.BaseTask;

import org.apache.tools.ant.BuildException;

public class GetDevicePropertyTask extends BaseTask {
    private String mLocalProperty;
    private String mRemoteProperty;
    
    public void setLocal(String name) {
        mLocalProperty = name;
    }
    
    public void setRemote(String name) {
        mRemoteProperty = name;
    }
    
    @Override
    public void execute() throws BuildException {
        if(mLocalProperty == null) {
            error("Local property name is not specified. Use \"local\" attribute.");
        }
        
        if(mRemoteProperty == null) {
            error("Remote property name is not specified. Use \"remote\" attribute.");
        }
        
        try {
            String value = getDevice().getPropertySync(mRemoteProperty);
            if(value != null) {
                info("Device property %s = %s", mRemoteProperty, value);
                getProject().setProperty(mLocalProperty, value);
            } else {
                info("Device property \"%s\" is not set.", mRemoteProperty);
            }
            
        } catch(Exception ex) {
            error("Failed to get property \"%s\" value  from device %s", 
                    mRemoteProperty, getDeviceSerialNumber());
        }
    }
}
