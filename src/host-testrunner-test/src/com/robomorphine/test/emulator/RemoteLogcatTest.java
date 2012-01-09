package com.robomorphine.test.emulator;

import com.robomorphine.test.TestManager;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.StdLog;

import java.io.File;

import junit.framework.TestCase;

public class RemoteLogcatTest extends TestCase {
    
    public void testLogcat() throws Exception {
        
        File path = new File("r:\\repository\\dev\\bin\\android-sdk");
        ILog logger = new StdLog();  
        TestManager testManager = new TestManager(path, logger);
        testManager.connectAdb();
        
        RemoteLogcat logcat = new RemoteLogcat(testManager.getAndroidDebugBridge(), 
                                    "304D1990904CFC6E", new File("bin/logcat.txt"), logger);
        logcat.addArg("-b asd");
        
        logcat.start(true);
        Thread.sleep(1000);
        logcat.stop();
    }
}
