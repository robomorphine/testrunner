package com.robomorphine.test.ant.device;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.robomorphine.test.ant.BaseTask;

import org.apache.tools.ant.BuildException;

public class LockDeviceTask extends BaseTask {
    
    private String mSerial;
    private boolean mVerify = true;
    private Boolean mIsEmulator = null;
    private boolean mUseFirst = false;
    
    public void setSerial(String serial) {
        mSerial = serial;
    }
    
    public void setVerify(boolean verify) {
        mVerify = verify;
    }
    
    public void setEmulator(boolean emulator) {
        if(mIsEmulator != null) {
            error("Cannot use both \"emulator\" and \"device\" attributes.");
        }
        mIsEmulator = emulator;
    }
    
    public void setDevice(boolean device) {
        if(mIsEmulator != null) {
            error("Cannot use both \"emulator\" and \"device\" attributes.");
        }
        mIsEmulator = !device;
    }
    
    public void setUseFirst(boolean lockFirst) {
        mUseFirst = lockFirst;
    }
    
    @Override
    public void execute() throws BuildException {
       
        if(mSerial == null) {
            info("Serial is not specified. Autolock is started... ");
            AndroidDebugBridge adb = getAdb();
            IDevice [] devices = adb.getDevices();
            if(devices.length == 0) {
                error("No devices are connected to adb.");
            }
            
            IDevice device = null;
            boolean multiple = false;
            for(IDevice curDevice : devices) {
                if(!curDevice.isOnline()) {
                    info("Skipping \"%s\" - it's not online.", curDevice.getSerialNumber());
                    continue;
                }
                
                boolean passed = false;
                if(mIsEmulator != null && mIsEmulator == curDevice.isEmulator()) {
                    passed = true;                            
                } else if(mIsEmulator == null) {
                    passed = true;
                }
                
                if(passed) {
                    info("Device \"%s\" can be locked.", curDevice.getSerialNumber());
                    if(device != null) {
                        multiple = true;
                    } else {
                        device = curDevice;
                    }
                }
            }
            
            if(device == null) {
                error("No devices satisified filter: isEmulator = %b", mIsEmulator);
            }
            
            if(multiple) {
                if(mUseFirst) {
                    warn("Multiple devices were available, locking first device.");
                } else {
                    error("More than one device is available. Can only lock single device.");
                }   
            }
            mSerial = device.getSerialNumber();
        }
        
        getContext().setDeviceSerialNumber(mSerial);
        if(mVerify) {
            /* will fail the task if device is not connected */
            getDevice();
        }
        info("Device is locked. Context is bound to device \"%s\".", mSerial);
    }
}
