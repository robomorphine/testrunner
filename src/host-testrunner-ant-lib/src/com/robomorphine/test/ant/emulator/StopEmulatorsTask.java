package com.robomorphine.test.ant.emulator;

import com.robomorphine.test.ant.BaseTask;
import com.robomorphine.test.emulator.EmulatorStopper;

import org.apache.tools.ant.BuildException;

public class StopEmulatorsTask extends BaseTask {
    
    @Override
    public void execute() throws BuildException {
        EmulatorStopper stopper = new EmulatorStopper(getTestManager());
        info("Stopping all emulators.");
        stopper.stopAll();
    }
}
