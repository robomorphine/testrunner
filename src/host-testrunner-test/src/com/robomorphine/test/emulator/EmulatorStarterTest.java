package com.robomorphine.test.emulator;

import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.internal.avd.AvdInfo;
import com.robomorphine.test.AdbConnectionException;
import com.robomorphine.test.AvdManager;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.StdLog;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import junit.framework.TestCase;

public class EmulatorStarterTest extends TestCase {
    
    public void testStartEmulator() throws AndroidLocationException, IOException,
            EmulatorStarterException, AdbConnectionException {
        
        File path = new File("r:\\repository\\dev\\bin\\android-sdk");
        ILog logger = new StdLog();        
        
        TestManager testManager = new TestManager(path, logger);
        testManager.connectAdb();
        
        AvdManager avd = testManager.getAvdManager();
        
        String avdName = "test-avd";
        AvdInfo info = avd.createAvd(avdName, "android-4", -1, new HashMap<String, String>(), false, true);
        
        EmulatorStarter starter = new EmulatorStarter(testManager);
        String serialNo = starter.start(avdName, new LinkedList<String>());
        assertNotNull(serialNo);
        
//        EmulatorStopper stopper = new EmulatorStopper(testManager);
//        stopper.stop(serialNo);
    }
    
    
    public void adbRestart() throws AndroidLocationException, IOException,
            EmulatorStarterException, AdbConnectionException {
        
        File path = new File("r:\\repository\\dev\\bin\\android-sdk");
        ILog logger = new StdLog();        
        
        TestManager testManager = new TestManager(path, logger);
        testManager.connectAdb();
        testManager.getAndroidDebugBridge().restart();
        testManager.connectAdb();
    }

}
