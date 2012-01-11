package com.robomorphine.test.emulator;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.exception.AdbConnectionException;
import com.robomorphine.test.exception.DeviceNotConnectedException;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.sdktool.EmulatorTool;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class EmulatorStarter {
    
    private final static String DEVICE_PROP_NAME = "rbm.starter.uuid";
    public final static long DEFAULT_CONNECT_TIMEOUT = 60 * 2 * 1000;
    public final static long DEFAULT_BOOT_TIMEOUT = 60 * 5 * 1000;
    public final static long DEFAULT_LOW_CPU_TIMEOUT = 60 * 1 * 1000;
    public final static int DEFAULT_LOW_CPU_THRESHOLD = 30; /* 0 - 100 */
    private final static long CPU_CHECK_INTERVAL = 5000;
    
    private long mConnectTimeout; 
    private long mBootTimeout;
    private long mLowCpuTimeout;
    private int mLowCpuThreshold;
    private final TestManager mTestManager;
    private final ILog mLog;
    
    public EmulatorStarter(TestManager testManager) {
        mTestManager = testManager;
        mLog = mTestManager.newPrefixedLogger(EmulatorStarter.class);
        mConnectTimeout = DEFAULT_CONNECT_TIMEOUT;
        mBootTimeout = DEFAULT_BOOT_TIMEOUT;
        mLowCpuTimeout = DEFAULT_LOW_CPU_TIMEOUT;
        mLowCpuThreshold = DEFAULT_LOW_CPU_THRESHOLD;
    }
    
    public long getConnectTimeout() {
        return mConnectTimeout;
    }
    
    public void setConnectTimeout(long timeout) {
        mConnectTimeout = timeout;
    }
    
    public long getBootTimeout() {
        return mBootTimeout;
    }
    
    public void setBootTimeout(long timeout) {
        mBootTimeout = timeout;
    }
    
    public long getLowCpuTimeout() {
        return mBootTimeout;
    }
    
    public void setLowCpuTimeout(long timeout) {
        mLowCpuTimeout = timeout;
    }
    
    public int getLowCpuThreshold() {
        return mLowCpuThreshold;
    }
    
    public void setLowCpuThreshold(int threshold) {
        mLowCpuThreshold = threshold;
    }
     
    private boolean reconnectEmulator(String serialNo) {
        mLog.v("Reconnecting to %s device by restarting adb.", serialNo);
        EmulatorConsole console = new EmulatorConsole(mTestManager);
        
        /* So device is disconnected from adb. 
         * Check if emulator is still running via emulator console */
         if(!console.consoleIsRunning(serialNo)) {
             /* so there is no more running emulator? failed! */             
             return false;
         }
         
         try {
             mLog.v("Restarting & reconnecting to adb...");
             mTestManager.reconnectAdb();
             mLog.v("Reconnected to adb.");
         } catch(AdbConnectionException ex) {
             mLog.v("Failed to restart or reconnected to adb.");
             return false;
         }
         
         AndroidDebugBridge adb = mTestManager.getAndroidDebugBridge();
         for(IDevice device : adb.getDevices()) {
             if(device.getSerialNumber().equals(serialNo)) {
                 mLog.v("Restarted adb, device %s is now connected again.", serialNo);
                 return true;
             }
         }
         
         mLog.e(null, "Restarted adb, but device %s was not found.", serialNo);
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
            String serial = waitForBoot(uuidPropName, uuidPropValue);
            if(mLowCpuTimeout > 0) {
                waitForLowCpu(serial);
            }
            return serial;
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
            mLog.e(null, "Emulator did not connect to adb for %s.", 
                              AdbDeviceWaiter.formatTime(mConnectTimeout));            
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
                mLog.v("Boot result: %s", bootSuccess?"success":"failure");
                break;
            } catch(DeviceNotConnectedException ex) {                
                if(!reconnectEmulator(serialNo)) {
                    break;
                }
            }
        }
        
        if(!bootSuccess) {
            mLog.e(null, "Emulator connected but failed to boot within %s.", 
                              AdbDeviceWaiter.formatTime(mBootTimeout));
            throw new EmulatorStarterException("Failed to wait for emulator to boot.");
        }
        return serialNo;
    }
    
    private String readProcStatCpuLine(String serialNo) throws EmulatorStarterException {
        AndroidDebugBridge adb = mTestManager.getAndroidDebugBridge();
        IDevice device = null;
        for(IDevice curDevice : adb.getDevices()) {
            if(curDevice.getSerialNumber().equals(serialNo)) {
                device = curDevice;
                break;
            }
        }
        if(device == null) {
            mLog.e(null, "Device %s is not found, failed to calculate cpu load.", serialNo);
            throw new EmulatorStarterException("Device not found");
        }
        
        final StringBuilder builder = new StringBuilder();
        try {
            device.executeShellCommand("cat /proc/stat", new IShellOutputReceiver() {
                @Override public boolean isCancelled() {return false;}                
                @Override public void flush() { /* ignored */ }                
                @Override public void addOutput(byte[] buf, int offset, int len) {
                    builder.append(new String(buf, offset, len));
                }
            });
        } catch(Exception ex) {
            mLog.e(ex, "Failed to get /proc/stats data from %s emulator.", serialNo);
        }
        
        String [] lines = builder.toString().split("\n");
        if(lines.length == 0) {
            return null;
        } else {
            return lines[0];
        }
    }
    
    public int getCpuLoad(String serialNo) throws EmulatorStarterException {

        /* read CPU data for the 1st time */
        String load = readProcStatCpuLine(serialNo);
        if(load == null) {
            return -1;
        }
        String[] toks = load.split(" ");
        if(toks.length < 8) {
            return -1;
        }
        long idle1 = Long.parseLong(toks[5]);
        long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
        
        try {
            /* wait a bit to have at least 2 samples of data */
            Thread.sleep(500);
        } catch(InterruptedException ex) {
            return -1;
        }
        
        /* read CPU data for the 2nd time */
        load =  readProcStatCpuLine(serialNo);
        if(load == null) {
            return -1;
        }
        toks = load.split(" ");
        if(toks.length < 8) {
            return -1;
        }
        
        long idle2 = Long.parseLong(toks[5]);
        long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
            + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

        /* calculate */
        long result = (cpu2 - cpu1) * 100 / ((cpu2 + idle2) - (cpu1 + idle1) + 1);
        return (int)result;
    }
    
    public void waitForLowCpu(String serialNo) throws EmulatorStarterException {
        long end = System.currentTimeMillis() + mLowCpuTimeout;
        int cpuLoadCounter = 0;
        int cpuLoadCounterTarget = 3;
        mLog.v("Waiting for low cpu (< %d%%) on emulator %s", mLowCpuThreshold, serialNo);
        while(true) {
            long timeout = end - System.currentTimeMillis();
            if(timeout <= 0) {
                break;
            }
   
            int cpu = getCpuLoad(serialNo);
            if(cpu < 0) {
                mLog.e(null, "Failed to read cpu load of \"%s\".", serialNo);
            } else {
                mLog.i("Emulator %s has cpu load %d%%, threshold %d%% (time left %s).", 
                           serialNo, cpu, mLowCpuThreshold, AdbDeviceWaiter.formatTime(timeout));
                if(cpu < mLowCpuThreshold) {
                    cpuLoadCounter++;
                    mLog.v("Score: %d", cpuLoadCounter);
                } else {
                    cpuLoadCounter=0;
                }
            }
                        
            if(cpuLoadCounter >= cpuLoadCounterTarget) {
                mLog.i("Emulator had low cpu (< %d%%) %d time in a row. Done waiting.", 
                           mLowCpuThreshold, cpuLoadCounterTarget);
                return;
            }
            
            try {
                Thread.sleep(CPU_CHECK_INTERVAL);
            } catch(InterruptedException ex){
                 return;
            }
        }        
        /* don't fail, just give it a chance */
        mLog.i("Timed out waiting for low cpu. Assuming that's ok to continue.");
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
