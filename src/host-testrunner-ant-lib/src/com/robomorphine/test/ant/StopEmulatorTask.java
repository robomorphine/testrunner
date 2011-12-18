package com.robomorphine.test.ant;

import com.robomorphine.test.emulator.EmulatorStopper;

import org.apache.tools.ant.BuildException;

public class StopEmulatorTask extends BaseTask {

    private String mSerialNo;
    
    public void setSerial(String serial) {
        mSerialNo = serial;
    }
    
    @Override
    public void execute() throws BuildException {
        if(mSerialNo == null) {
            error("Serial number is not specified.");
        }
        
        EmulatorStopper stopper = new EmulatorStopper(getTestManager());
        if(!stopper.stop(mSerialNo)) {
            error("Failed to stop emulator.");
        }
    }
}
