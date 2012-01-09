package com.robomorphine.test.emulator;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.exception.DeviceNotConnectedException;
import com.robomorphine.test.log.ILog;

import java.io.IOException;

class AdbDeviceWaiter implements IDeviceChangeListener {
    
    private final static String SDK_VER_NAME = "ro.build.version.sdk";
    private final static String DEV_BOOT_COMPLETED_PROP_NAME = "dev.bootcomplete";
    private final static String SYS_BOOT_COMPLETED_PROP_NAME = "sys.boot_completed";
    private final static String BOOT_ANIM_SVC_PROP_NAME = "init.svc.bootanim";
    
    private final static long CONNECTED_CHECK_INTERVAL = 5000;
    private final static long BOOT_COMPLETED_CHECK_INTERVAL = 5000; 
    
    private final TestManager mTestManager;
    private final ILog mLog;    
    private final Object mSync = new Object();
    
    public AdbDeviceWaiter(TestManager testManager) {
        mTestManager = testManager;
        mLog = mTestManager.newPrefixedLogger(AdbDeviceWaiter.class);
    }
    
    public static String formatTime(long ms) {
        return String.format("%02d:%02d", ms/1000/60, ms/1000 % 60);
    }
    
    private void notifyChanged() {
        synchronized(mSync) {
            mSync.notifyAll();
        }
    }    
    
    @Override
    public void deviceConnected(IDevice device) {
        mLog.v("Device connected: %s", device.getSerialNumber());
        notifyChanged();
    }
            
    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        mLog.v("Device changed: %s", device.getSerialNumber());
        notifyChanged();
    }
    
    @Override
    public void deviceDisconnected(IDevice device) {
        mLog.v("Device disconnected: %s", device.getSerialNumber());
        notifyChanged();            
    }
    
    private void printGetPropError(IDevice device, Throwable ex, String msg) {
        msg = String.format("Device %s: failed to get property value, %s",
                             device.getSerialNumber(), msg);
        mLog.e(ex, msg);
    }
    
    private IDevice findByUUID(String uuidPropName, String uuidPropValue) {
        for(IDevice device : mTestManager.getAndroidDebugBridge().getDevices()) {
            try {
                if(device.isOffline()) {
                    mLog.v("Device %s is offline. Skipped.", device.getSerialNumber());
                    continue;
                }
                
                String value = device.getPropertySync(uuidPropName);
                mLog.v("Device %s has uuid %s", device.getSerialNumber(), value);
                if(uuidPropValue.equals(value)) {
                    mLog.v("Device %s is a match!", device.getSerialNumber());
                    return device;
                }
            } catch(ShellCommandUnresponsiveException ex) {
                printGetPropError(device, ex, "Shell unresponsive.");
            } catch(AdbCommandRejectedException ex) {
                printGetPropError(device, ex, "Adb cmd rejected.");
            } catch(IOException ex) {
                printGetPropError(device, ex, "IO error.");
            } catch(TimeoutException ex) {
                printGetPropError(device, ex, "Timeout.");
            }
        }
        return null;
    }
    
    private IDevice findBySerialNo(String serialNo) {
        for(IDevice device : mTestManager.getAndroidDebugBridge().getDevices()) {
            if(device.getSerialNumber().equals(serialNo)) {
                return device;
            }
        }
        return null;
    }
    
    public String waitForDeviceToConnect(long timeout, String uuidPropName, String uuidPropValue) {
        try {
            mLog.v("Registered for ADB device notifications");
            AndroidDebugBridge.addDeviceChangeListener(this);                
            return _waitForDeviceToConnect(timeout, uuidPropName, uuidPropValue);
        } finally {
            AndroidDebugBridge.removeDeviceChangeListener(this);
        }
    }
    
    private String _waitForDeviceToConnect(long timeout, String uuidPropName, String uuidPropValue) {
        mLog.v("Waiting for device to connect with property: %s=%s (timeout: %s)",
                  uuidPropName, uuidPropValue, formatTime(timeout));
        
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis() + timeout;
        while(System.currentTimeMillis() < end) {
            IDevice device = findByUUID(uuidPropName, uuidPropValue);
            if(device != null) {
                long elapsed = System.currentTimeMillis() - start;
                mLog.i("Device is now connected to adb: %s (elapsed: %s).", 
                           device.getSerialNumber(), formatTime(elapsed));
                return device.getSerialNumber();
            }
            
            long delta = end - System.currentTimeMillis();            
            mLog.i("Device is still not connected to adb (left: %s)", formatTime(delta));
            
            if(delta < 0) break;
            if(delta > CONNECTED_CHECK_INTERVAL) {
                delta = CONNECTED_CHECK_INTERVAL;
            }
            
            synchronized(mSync) {
                try {
                    mSync.wait(delta);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
        return null;
    }
    
    public boolean waitForDeviceToBoot(long timeout, String serialNo)
            throws DeviceNotConnectedException {
        
        try {
            mLog.v("Registered for ADB device notifications.");
            AndroidDebugBridge.addDeviceChangeListener(this);                
            return _waitForDeviceToBoot(timeout, serialNo);
        } finally {
            AndroidDebugBridge.removeDeviceChangeListener(this);
        }
    }
    
    boolean getDevBootedStatus(IDevice device) throws ShellCommandUnresponsiveException,
            AdbCommandRejectedException, IOException, TimeoutException {
        
        String devBootValue = device.getPropertySync(DEV_BOOT_COMPLETED_PROP_NAME);
        if(devBootValue == null) {
            return false;
        }
        
        int state = 0;
        try {
            state = Integer.parseInt(devBootValue.trim());
        } catch(NumberFormatException ex) {
            mLog.w("Failed to parse %s as dev boot status!", devBootValue);
        }
        return state != 0;
    }
    
    boolean getSysBootedStatus(IDevice device) throws ShellCommandUnresponsiveException,
            AdbCommandRejectedException, IOException, TimeoutException {
        
        String sysBootValue = device.getPropertySync(SYS_BOOT_COMPLETED_PROP_NAME);
        if(sysBootValue == null) {
            return false;
        }
        
        int state = 0;
        try {
            state = Integer.parseInt(sysBootValue.trim());
        } catch(NumberFormatException ex) {
            mLog.w("Failed to parse %s as sys boot status!", sysBootValue);
        }
        return state != 0;
    }
    
    int getSdkLevel(IDevice device) throws ShellCommandUnresponsiveException,
            AdbCommandRejectedException, IOException, TimeoutException {
        
        String levelValue = device.getPropertySync(SDK_VER_NAME);
        if(levelValue == null) {
            return -1;
        }
        
        int level = -1;
        try {
            level = Integer.parseInt(levelValue.trim());
        } catch(NumberFormatException ex) {
            mLog.w("Failed to parse %s as sdk level!", levelValue);
        }
        return level;
    }
    
    boolean isBootAnimStopped(IDevice device) throws ShellCommandUnresponsiveException,
            AdbCommandRejectedException, IOException, TimeoutException {
        String stateValue = device.getPropertySync(BOOT_ANIM_SVC_PROP_NAME);
        if(stateValue == null) {
            return false;
        }
        String expectedState = "stopped".trim().toLowerCase();
        String currentState = stateValue.trim().toLowerCase();
        return expectedState.equals(currentState);
    }
    
    private boolean isBootCompleted(IDevice device) {
        String serialNo = device.getSerialNumber();
        try {
            if(device.isOffline()) {
                mLog.v("Device %s is offline.", serialNo); 
                return false;
            }
            
            if(!getDevBootedStatus(device)) {
                mLog.v("Device %s is not booted.", serialNo);
                return false;
            }
            
            boolean sysbooted = true;//always true for API Level < 10
            int level = getSdkLevel(device);            
            if(level >= 10) {
                sysbooted = getSysBootedStatus(device);                              
            } else if(level < 0) {
                mLog.v("Failed to detect sdk level of %s device.", serialNo);
            }
            
            if(!sysbooted) {
                mLog.v("Device %s is booted, but system is not yet booted", serialNo);
                return false;
            }
            
            if(!isBootAnimStopped(device)) {
                mLog.v("Device %s is booted, system is booted, but boot animation is still running.", 
                          serialNo);
                return false;
            }
            
            return true;
            
        } catch(ShellCommandUnresponsiveException ex) {
            printGetPropError(device, ex, "Shell unresponsive.");
        } catch(AdbCommandRejectedException ex) {
            printGetPropError(device, ex, "Adb cmd rejected.");
        } catch(IOException ex) {
            printGetPropError(device, ex, "IO error.");
        } catch(TimeoutException ex) {
            printGetPropError(device, ex, "Timeout.");
        }
        return false;
    }
    
    private int getDeviceProcessCount(IDevice device) {
        final StringBuilder result = new StringBuilder();
        try {
            device.executeShellCommand("ps", new IShellOutputReceiver() {                
                @Override
                public boolean isCancelled() {
                    return false;
                }
                
                @Override
                public void flush() {
                }
                
                @Override
                public void addOutput(byte[] data, int offset, int length) {
                    result.append(new String(data, offset, length));
                }
            });
            
        } catch(Exception ex) {
            return -1;
        }
        return result.toString().split("\n").length - 1;
    }
    
    private boolean _waitForDeviceToBoot(long timeout, String serialNo)
            throws DeviceNotConnectedException {
        
        mLog.v("Waiting for device %s to boot (timeout: %s).", serialNo, formatTime(timeout));
        
        long start = System.currentTimeMillis();                
        long end = System.currentTimeMillis() + timeout;
        while(System.currentTimeMillis() < end) {
            IDevice device = findBySerialNo(serialNo);
            if(device == null) {
                mLog.e(null, "Device %s is not connected to adb.", serialNo);
                throw new DeviceNotConnectedException();
            }
            
            if(isBootCompleted(device)) {
                long elapsed = System.currentTimeMillis() - start;
                mLog.i("Device %s has booted (elapsed: %s).", serialNo, formatTime(elapsed));
                return true;
            }
            
            long delta = end - System.currentTimeMillis();
            mLog.i("Device %s is still not fully booted (processes: %d, left: %s)", 
                       serialNo, getDeviceProcessCount(device), formatTime(delta));            
            
            if(delta < 0) break;            
            if(delta > BOOT_COMPLETED_CHECK_INTERVAL) {
                delta = BOOT_COMPLETED_CHECK_INTERVAL;
            }
            
            synchronized(mSync) {
                try {
                    mSync.wait(delta);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
        
        return false;
    }
}