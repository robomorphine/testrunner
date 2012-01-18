package com.robomorphine.test.emulator;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.sdktool.AdbTool;
import com.robomorphine.test.sdktool.SdkTool.Result;
import com.robomorphine.test.sdktool.ToolsManager;

public class EmulatorStopper {  // NOPMD 
    
    private final static long DEFAULT_TIMEOUT = 3000;
    
    private final ILog mLog;
    private final EmulatorConsole mConsole;
    private final TestManager mTestManager;
            
    public EmulatorStopper(TestManager testManager) {
        mLog = testManager.newPrefixedLogger(EmulatorStopper.class);
        mConsole = new EmulatorConsole(testManager);
        mTestManager = testManager;
    }
    
    /*******************************************/
    /**         Running Detection             **/      
    /*******************************************/
    
    /**
     * Detects if emulator is running using adb.
     * @param serialNo
     * @return true if running, false otherwise.
     */
    public boolean adbIsRunning(String serialNo) {
        AndroidDebugBridge adb = mTestManager.getAndroidDebugBridge();
        for(IDevice device : adb.getDevices()) {
            if(device.getSerialNumber().endsWith(serialNo)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isRunning(String serialNo) {
        return adbIsRunning(serialNo) || mConsole.consoleIsRunning(serialNo);
    }
    
    /*******************************************/
    /**                Stop                   **/      
    /*******************************************/
    
    public boolean adbStop(String serialNo) {
       ToolsManager toolsManager = mTestManager.getToolsManager();
       AdbTool adbTool = toolsManager.createAdbTool();
       
       adbTool.addArgument("-s", serialNo, "emu", "kill");
       try {
           Result result = adbTool.execute();
           return result.getExitCode() == 0;
       } catch(Exception ex) {
           mLog.e(ex, "Failed to stop emulator \"%s\" using adb.", serialNo);
           return false;
       }
    }
    
    public boolean waitForEmualtorStopped(String serialNo, long ms) {
        mLog.v("Waiting for emulator \"%s\" to stop.", serialNo);
        long end = System.currentTimeMillis() + ms;
        long sleepStep = 1000;
        while(end > System.currentTimeMillis()) {
            if(!isRunning(serialNo)) {
                return true;
            }
            mLog.v("Emulator \"%s\" is still alive...", serialNo);
            
            long delta = end - System.currentTimeMillis();
            if(delta <= 0) {
                return false;
            }
            
            long sleep = sleepStep;
            if(sleep > delta) {
                sleep = delta;
            }            
            
            try {
                Thread.sleep(sleep);
            } catch(InterruptedException ex) {
                return false;
            }
        }
        return false;
    }    
    
    public boolean stop(String serialNo) {  // NOPMD 
        /* check adb connection */
        if(!mTestManager.isAdbConnected()) {
            throw new IllegalStateException("ADB is not connected");
        }
        
        if(!isRunning(serialNo)) {
            mLog.v("Emualtor \"%s\" is not running. Nothing to stop.", serialNo);
            return true;
        }
        
        /* verify serial represents emulator, and not a real device */
        AndroidDebugBridge adb = mTestManager.getAndroidDebugBridge();
        boolean skipAdbStop = true;
        for(IDevice curDevice : adb.getDevices()) {
            if(curDevice.getSerialNumber().endsWith(serialNo)) {
                if(!curDevice.isEmulator()) {
                    throw new IllegalArgumentException("Not an emulator: " + serialNo);
                }
                skipAdbStop = false;                
                break;
            }
        }
        
        if(!skipAdbStop) {  // NOPMD 
            /* Killing emulator using adb's embedded control over emulator console */        
            mLog.v("Killing emulator \"%s\" using adb.", serialNo); 
            if(adbStop(serialNo)) {
                if(waitForEmualtorStopped(serialNo, DEFAULT_TIMEOUT)) {
                    mLog.v("Emulator \"%s\" is no longer running.", serialNo);
                    return true;
                }        
            } else {
                mLog.v("Failed to stop emulator \"%s\" using avd.", serialNo);                  
            }        
        } else {
            mLog.w("Emulator \"%s\" is not visible from adb.", serialNo);
        }
        
        /* Let's kill emulator using emulator console */        
        mLog.v("Killing emulator \"%s\" using emulator console.", serialNo); 
        if(mConsole.consoleStop(serialNo)) {
            if(waitForEmualtorStopped(serialNo, DEFAULT_TIMEOUT)) {
                mLog.v("Emulator \"%s\" is no longer running.", serialNo);
                return true;
            }
        } else {
            mLog.v("Failed to stop emulator \"%s\" using emulator console.", serialNo);
        }
        
        mLog.e(null, "I really tried to kill \"%s\" emulator. But failed.", serialNo);        
        return false;
    }
    
    public boolean stopMultiple(int firstPort, int lastPort) {        
        if(firstPort % 2 != 0) {
            firstPort++;
        }
        
        mLog.v("Starting to kill emulators on ports %d - %d.", firstPort, lastPort);
        
        boolean ok = true;
        for(int i = firstPort; i <= lastPort; i+=2) {
            String serialNo = String.format("emulator-%d", i); 
            if(isRunning(serialNo)) {
                mLog.i("Port %d: emulator detected, stopping...", i);
                if(stop(serialNo)) {  // NOPMD 
                    mLog.i("Port %d: emulator stopped.", i);
                } else {
                    ok = false;
                    mLog.w("Port %d: failed to stop emulator.", i);
                }
            } else {
                mLog.i("Port %d: no emulator detected.", i);
            }
        }
        
        return ok;
    }
    
    public boolean stopAll() {
        mLog.v("Killing all running emulators...");
        return stopMultiple(5554, 5584);
    }
}
