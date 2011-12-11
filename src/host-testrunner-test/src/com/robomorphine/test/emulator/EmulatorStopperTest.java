package com.robomorphine.test.emulator;

import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.robomorphine.test.AdbConnectionException;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.StdLog;

import java.io.File;

import junit.framework.TestCase;

public class EmulatorStopperTest extends TestCase {
    
    public void testStopEmulator() throws AndroidLocationException, AdbConnectionException {
        File path = new File("r:\\repository\\dev\\bin\\android-sdk");
        ILog logger = new StdLog();        
        
        TestManager testManager = new TestManager(path, logger);
        testManager.connectAdb();
        
        EmulatorStopper stopper = new EmulatorStopper(testManager);
        assertTrue(stopper.stop("emulator-5554"));
        
        //assertTrue(stopper.stopAll());
    }
}
