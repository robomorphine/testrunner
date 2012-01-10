package com.robomorphine.test.ant;

import com.robomorphine.test.TestManager;
import com.robomorphine.test.exception.AdbConnectionException;

import org.apache.tools.ant.BuildException;

public class RestartAdbTask extends BaseTask {
    
    @Override
    public void execute() throws BuildException {
        TestManager testManager = getTestManager(); 
        if(!testManager.getAndroidDebugBridge().restart()) {
            warn("Failed to restart adb.");
        } else {
            try {
                testManager.connectAdb();
            } catch(AdbConnectionException ex) {
                error(ex, "Failed to connect to adb.");
            }
            info("Successfuly restarted adb.");
        }
    }

}
