package com.robomorphine.test;
import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.internal.avd.AvdInfo;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.StdLog;
import com.robomorphine.test.util.AvdHelper;

import java.io.File;
import java.util.HashMap;

import junit.framework.TestCase;

public class AvdManagerTest extends TestCase {
    
    public void testCreateAvd() throws AndroidLocationException {
        File path = new File("r:\\repository\\dev\\bin\\android-sdk");
        ILog logger = new StdLog();
        
        TestManager testManager = new TestManager(path, logger);                               
        AvdHelper avdManager = new AvdHelper(testManager);
        
        HashMap<String, String> config = new HashMap<String, String>();
        config.put("hw.ramSize", "1024");
        
        AvdInfo avdInfo = avdManager.createAvd("test-name2", "android-14", 
                                                100, config, 
                                                false, true);
        assertNotNull(avdInfo);
    }
    
    public void testCreateDeleteAvd() throws AndroidLocationException {
        File path = new File("r:\\repository\\dev\\bin\\android-sdk");
        ILog logger = new StdLog();
        
        TestManager testManager = new TestManager(path, logger);                               
        AvdHelper avdManager = new AvdHelper(testManager);
        
        HashMap<String, String> config = new HashMap<String, String>();       
        AvdInfo avdInfo = avdManager.createAvd("test-name2", "android-14", 
                                                100, config, 
                                                false, true);
        assertNotNull(avdInfo);
        assertTrue(avdManager.deleteAvd(avdInfo.getName()));
    }

}
