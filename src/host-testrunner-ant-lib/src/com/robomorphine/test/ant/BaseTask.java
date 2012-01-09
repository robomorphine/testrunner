package com.robomorphine.test.ant;



import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.sdklib.SdkManager;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.exception.AdbConnectionException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class BaseTask extends Task {
    
    protected static final String DEFAULT_CONTEXT_REF_NAME = "rbm-context";
     
    private String mContextRefName = DEFAULT_CONTEXT_REF_NAME;
    private String mDeviceSerialNumber;
    private Context mContext;
    
    public void error(String format, Object...args) {
        error(null, format, args);        
    }
    
    public void error(Throwable ex, String format, Object...args) {
        String msg = String.format(format, args);
        throw new BuildException(msg, ex, getLocation());
    }
    
    public void warn(String format, Object...args) {
        log(String.format(format, args), Project.MSG_WARN);        
    }
    
    public void info(String format, Object...args) {
        log(String.format(format, args), Project.MSG_INFO);        
    }
    
    public void dbg(String format, Object...args) {
        log(String.format(format, args), Project.MSG_DEBUG);        
    }
    
    public void verbose(String format, Object...args) {
        log(String.format(format, args), Project.MSG_VERBOSE);        
    }
    
    public void setContextRef(String ref) {
        mContextRefName = ref;
    }
    
    public void setSerial(String deviceSerial) {
        mDeviceSerialNumber = deviceSerial;
    }
    
    private Context getContextUnchecked() {
        if(mContext != null) return mContext;
        String refName = DEFAULT_CONTEXT_REF_NAME;
        if(mContextRefName != null) {
            refName = mContextRefName;
        }
        return (Context)getProject().getReference(refName);
    }
    
    public Context getContext() {
        if(mContext != null) return mContext;        
        mContext = getContextUnchecked();
        if(mContext == null) {
           error("Context reference is not set. Make sure you've called setup task.");
        }
        return mContext;
    }
    
    public TestManager getTestManager() {
        TestManager manager = getContext().getTestManager();
        if(manager == null) {
            error("TestManager is not set.");
        }
        return manager;
    }
    
    public AndroidDebugBridge getAdb() {
        TestManager manager = getTestManager();
        AndroidDebugBridge adb = manager.getAndroidDebugBridge();
        if(adb == null) {
            error("ADB is not connected.");
        }
        return adb;
    }
    
    public SdkManager getSdkManager() {
        TestManager manager = getTestManager();
        return manager.getSdkManager();
    }
    
    public String getDeviceSerialNumber() {
        if(mDeviceSerialNumber != null) {
            return mDeviceSerialNumber;
        }
        
        String serialNo = getContext().getDeviceSerialNumber();
        if(serialNo == null) {
            error("Device serial number is not set.");
        }
        return serialNo;
    }
    
    public IDevice getDevice() {
        String serialNo = getDeviceSerialNumber();
        if(serialNo == null) {
            error("No device is locked to context.");
        }
        
        AndroidDebugBridge adb = getAdb();
        
        for(IDevice device : adb.getDevices()) {
            if(device.getSerialNumber().equals(serialNo)) {
                return device;
            }
        }
        error("Device \"%s\" is not connected to ADB.", serialNo);
        return null;
    }
    
    @Override
    public void maybeConfigure() throws BuildException {
        Context context = getContextUnchecked();
        if(context != null) {
            TestManager testManager = context.getTestManager();
            if(testManager != null && !testManager.isAdbConnected()) {            
                dbg("Connecting to adb (lazy setup is in effect).");
                try {
                    context.getTestManager().connectAdb();
                } catch(AdbConnectionException ex) {
                    error(ex, "Deferred ADB connect: failed to connect to adb.");
                }
            }
        }
        super.maybeConfigure();
    }
    
}
