package com.robomorphine.test.ant.device;

import com.robomorphine.test.ant.BaseTask;

import org.apache.tools.ant.BuildException;

public class UnlockDeviceTask extends BaseTask {
    
    @Override
    public void execute() throws BuildException {
        getContext().setDeviceSerialNumber(null);
        info("Device unlocked. Context is not bound to any device.");
    }
}
