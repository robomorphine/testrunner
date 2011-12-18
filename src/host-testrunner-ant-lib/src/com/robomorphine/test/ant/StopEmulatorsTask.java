package com.robomorphine.test.ant;

import com.robomorphine.test.emulator.EmulatorStopper;

import org.apache.tools.ant.BuildException;

public class StopEmulatorsTask extends BaseTask {
    
    @Override
    public void execute() throws BuildException {
        EmulatorStopper stopper = new EmulatorStopper(getTestManager());
        stopper.stopAll();
    }
}
