package com.robomorphine.test.emulator;

import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.internal.avd.AvdInfo;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.exception.AdbConnectionException;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.StdSdkLogger;
import com.robomorphine.test.util.AvdHelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import junit.framework.TestCase;

public class EmulatorStarterTest extends TestCase {
    
    public void testStartEmulator() throws AndroidLocationException, IOException,
            EmulatorStarterException, AdbConnectionException {
        
        File path = new File("r:\\repository\\dev\\bin\\android-sdk");
        ILog logger = new StdSdkLogger();        
        
        TestManager testManager = new TestManager(path, logger);
        testManager.connectAdb();
        
        AvdHelper avd = new AvdHelper(testManager);
        
        String avdName = "test-avd";
        AvdInfo info = avd.createAvd(avdName, "android-15", -1, new HashMap<String, String>(),
                    false, true);
        //assertNotNull(info);
        
        EmulatorStarter starter = new EmulatorStarter(testManager);
        String serialNo = starter.start(3, avdName, new LinkedList<String>());
        assertNotNull(serialNo);
        
//        EmulatorStopper stopper = new EmulatorStopper(testManager);
//        stopper.stop(serialNo);
    }
    
    public void testWaitForLowCput() throws Exception {
        File path = new File("r:\\repository\\dev\\bin\\android-sdk");
        ILog logger = new StdSdkLogger();        
        
        TestManager testManager = new TestManager(path, logger);
        testManager.connectAdb();
        EmulatorStarter starter = new EmulatorStarter(testManager);
        starter.setLowCpuThreshold(50);
        starter.waitForLowCpu("304D1990904CFC6E");
    }
    
    public void adbRestart() throws AndroidLocationException, IOException,
            EmulatorStarterException, AdbConnectionException {
        
        File path = new File("r:\\repository\\dev\\bin\\android-sdk");
        ILog logger = new StdSdkLogger();        
        
        TestManager testManager = new TestManager(path, logger);
        testManager.connectAdb();
        testManager.getAndroidDebugBridge().restart();
        testManager.connectAdb();
    }

}
