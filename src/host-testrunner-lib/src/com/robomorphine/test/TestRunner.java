package com.robomorphine.test;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.robomorphine.test.emulator.DeviceNotConnectedException;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.PrefixedLog;
import com.robomorphine.test.sdktool.AaptTool;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class TestRunner {
    
    private final ILog mLog;
    private final TestManager mTestManager;
    private final ITestRunListener mTestRunListener = new ITestRunListener() {
        
        public void testRunStarted(String runName, int testCount) {
            mLog.info("Test run started: %s ( %d ) ", runName, testCount);
        }
        
        public void testStarted(TestIdentifier test) {
            mLog.info("Test started: %s", test.toString());
        }
        
        public void testFailed(TestFailure status, TestIdentifier test, String trace) {
            mLog.info("Test failed: %s ( %s ). %s", status, test, trace);
        }
        
        public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
            mLog.info("Test ended: %s ( %s ).", test, testMetrics);
        }
        
        public void testRunFailed(String errorMessage) {
            mLog.info("Test run failed: %s.", errorMessage);
        }
        
        public void testRunStopped(long elapsedTime) {
            mLog.info("Test run stopped: %d.", elapsedTime);
        }
        
        public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
            mLog.info("Test run ended: %d (%s).", elapsedTime, runMetrics);
        }
    };
    
    public TestRunner(TestManager testManager) {
        mTestManager = testManager;
        mLog = new PrefixedLog(TestRunner.class.getSimpleName(), mTestManager.getLogger());
    }
    
    public void run(String serialNo, File testerApk, File testedApk)
            throws DeviceNotConnectedException, InstallException, IOException, TimeoutException,
            ShellCommandUnresponsiveException, AdbCommandRejectedException {
        
        AndroidDebugBridge adb = mTestManager.getAndroidDebugBridge();
        IDevice device = null;
        for(IDevice curDevice : adb.getDevices()) {
            if(curDevice.getSerialNumber().equals(serialNo)) { 
                device = curDevice;
                break;
            }
        }
    
        if(device == null) {
            throw new DeviceNotConnectedException(serialNo);
        }
        
        AaptTool aapt = mTestManager.getToolsManager().createAaptTool();
        String testerPackageName = aapt.getPacakgeName(testerApk);
        
        if(testerPackageName == null) {
            String msg = "Failed to extract package name from " + testerApk.getName();
            throw new InstallException(msg, null);
        }
        
        ApkManager apkManager = mTestManager.getApkManager();
        apkManager.uninstall(device, testedApk);
        apkManager.uninstall(device, testerApk);
        apkManager.install(device, testedApk, false);
        apkManager.install(device, testerApk, false);        
        
        RemoteAndroidTestRunner remoteRunner = new RemoteAndroidTestRunner(testerPackageName, device);
        remoteRunner.run(mTestRunListener);
    }
}
