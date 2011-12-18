package com.robomorphine.test.emulator;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.robomorphine.test.AdbConnectionException;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.PrefixedLog;
import com.robomorphine.test.sdktool.EmulatorTool;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class EmulatorStarter {
    
    private final static String DEVICE_PROP_NAME = "rbm.starter.uuid";
    public final static long DEFAULT_CONNECT_TIMEOUT = 60 * 2 * 1000;
    public final static long DEFAULT_BOOT_TIMEOUT = 60 * 5 * 1000;
    
    private final long mConnectTimeout; 
    private final long mBootTimeout;
    private final TestManager mTestManager;
    private final ILog mLog;
    
    public EmulatorStarter(TestManager testManager, long connectTimeout, long bootTimeout) {
        mTestManager = testManager;
        mLog = new PrefixedLog(EmulatorStarter.class.getSimpleName(), mTestManager.getLogger());
        mConnectTimeout = connectTimeout;
        mBootTimeout = bootTimeout;
    }
    
    public EmulatorStarter(TestManager testManager) {
        this(testManager, DEFAULT_CONNECT_TIMEOUT, DEFAULT_BOOT_TIMEOUT);
    }
     
    private boolean reconnectEmulator(String serialNo) {
        mLog.info("Reconnecting to %s device by restarting adb.", serialNo);
        EmulatorConsole console = new EmulatorConsole(mTestManager);
        
        /* So device is disconnected from adb. 
         * Check if emulator is still running via emulator console */
         if(!console.consoleIsRunning(serialNo)) {
             /* so there is no more running emulator? failed! */             
             return false;
         }
         
         try {
             mLog.info("Restarting & reconnecting to adb...");
             mTestManager.reconnectAdb();
             mLog.info("Reconnected to adb.");
         } catch(AdbConnectionException ex) {
             mLog.info("Failed to restart or reconnected to adb.");
             return false;
         }
         
         AndroidDebugBridge adb = mTestManager.getAndroidDebugBridge();
         for(IDevice device : adb.getDevices()) {
             if(device.getSerialNumber().equals(serialNo)) {
                 mLog.info("Restarted adb, device %s is now connected again.", serialNo);
                 return true;
             }
         }
         
         mLog.error(null, "Restarted adb, but device %s was not found.", serialNo);
         return false;
    }
    
    /**
     * @param avdName
     * @return serial number of started emulator
     */
    public String start(String avdName, List<String> emulatorArgs) throws IOException,
            EmulatorStarterException {    
        UUID uuid = UUID.randomUUID();
        String uuidPropName = DEVICE_PROP_NAME;
        String uuidPropValue = uuid.toString();
        String uuidProp = String.format("%s=%s", uuidPropName, uuidPropValue);
                
        EmulatorTool emulator = mTestManager.getToolsManager().createEmulatorTool();
        emulator.addArgument("@"+avdName);
        emulator.addArgument("-prop");
        emulator.addArgument(uuidProp);
        emulator.addArgument(emulatorArgs);
        emulator.startExecution();
        
        try{
            return waitForBoot(uuidPropName, uuidPropValue);
        } catch(EmulatorStarterException ex) {
            emulator.terminate();
            throw ex;
        }
    }
    
    private String waitForBoot(String uuidPropName, String uuidPropValue)
            throws EmulatorStarterException {
        
        /* wait for device to be usable by adb (i.e. connected and not in offline mode) */        
        AdbDeviceWaiter waiter = new AdbDeviceWaiter(mTestManager);        
        
        String serialNo = waiter.waitForDeviceToConnect(mConnectTimeout, uuidPropName, uuidPropValue);
        if(serialNo == null) {
            mLog.error(null, "Emulator did not connect to adb for %d ms.", mConnectTimeout);            
            throw new EmulatorStarterException("Emulator was started, but failed to connect to adb.");
        }
        
        boolean bootSuccess = false;
        long end = System.currentTimeMillis() + mBootTimeout;
        while(true) {
            try {
                long timeout = end - System.currentTimeMillis();
                if(timeout <= 0) {
                    break;
                }
                
                bootSuccess = waiter.waitForDeviceToBoot(timeout, serialNo);
                mLog.info("Boot result: %s", bootSuccess?"success":"failure");
                break;
            } catch(DeviceNotConnectedException ex) {                
                if(!reconnectEmulator(serialNo)) {
                    break;
                }
            }
        }
        
        if(!bootSuccess) {
            mLog.error(null, "Emulator connected but failed to boot within %d ms.", mBootTimeout);
            throw new EmulatorStarterException("Failed to wait for emulator to boot.");
        }
        return serialNo;
    }
    
    public String start(int tries, String avdName, List<String> emulatorArgs) throws IOException,
            EmulatorStarterException {
        
        if(tries == 0) {
            throw new EmulatorStarterException("Zero tries to start emulator!");
        }
        
        EmulatorStarterException lastEx = null;
        for(int i = 0; i < tries; i++) {
            try {
                return start(avdName, emulatorArgs);
            } catch(EmulatorStarterException ex) {
                lastEx = ex;
            }
        }
        throw lastEx;
    }
}
