package com.robomorphine.test;

import com.android.ddmlib.InstallException;
import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.robomorphine.test.exception.AdbConnectionException;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.StdSdkLogger;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class ApkManagerTest extends TestCase {
    
    public void testDecodeAndroidManifest() throws AndroidLocationException, AdbConnectionException, IOException, InstallException {
        File path = new File("r:\\repository\\dev\\bin\\android-sdk");
        ILog logger = new StdSdkLogger();        
        
        TestManager testManager = new TestManager(path, logger);
        testManager.connectAdb();
        
        ApkManager apkManager = testManager.getApkManager();
        
        File file = new File("r:\\repository\\dev\\src\\projects\\github\\robomorphine-testrunner\\bin\\debug\\com.robomorphine.testrunner.device.test.apk");
        apkManager.install("emulator-5554", file, false);
        apkManager.uninstall("emulator-5554", file);
        apkManager.install("emulator-5554", file, false);
        apkManager.install("emulator-5554", file, true);
        
    }
}
