package com.robomorphine.test.ant.device;

import com.robomorphine.test.ant.BaseTask;

import org.apache.tools.ant.BuildException;

public class LockDeviceTask extends BaseTask {
    
    private String mSerial;
    private boolean mVerify = true;
    
    public void setSerial(String serial) {
        mSerial = serial;
    }
    
    public void setVerify(boolean verify) {
        mVerify = verify;
    }
    
    @Override
    public void execute() throws BuildException {
        getContext().setDeviceSerialNumber(mSerial);
        if(mVerify) {
            /* will fail the task if device is not connected */
            getDevice();
        }
        info("Device is locked. Context is bound to device \"%s\".", mSerial);
    }
}
