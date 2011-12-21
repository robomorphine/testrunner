package com.robomorphine.test.ant.device;

import com.robomorphine.test.ant.BaseTask;

import org.apache.tools.ant.BuildException;

public class GetLockedDeviceTask extends BaseTask {
    
    private String mPropertyName;
    public void setProperty(String name) {
        mPropertyName = name;
    }
    
    @Override
    public void execute() throws BuildException {
        if(mPropertyName == null) {
            error("Property name is not speicifed. Use \"property\".");
        }
        
        String serial = getContext().getDeviceSerialNumber();
        if(serial == null) {
            error("Device is not locked!");
        }
        
        getProject().setProperty(mPropertyName, serial);
                
    }
}
