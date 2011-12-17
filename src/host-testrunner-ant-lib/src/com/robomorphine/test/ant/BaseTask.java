package com.robomorphine.test.ant;



import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.sdklib.SdkManager;
import com.robomorphine.test.TestManager;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class BaseTask extends Task {
    
    protected static final String DEFAULT_CONTEXT_REF_NAME = "rbm-context";
     
    private String mContextRefName = DEFAULT_CONTEXT_REF_NAME;
    private String mDeviceSerialNumber;
    private Context mContext;
    
    public void setContextRef(String ref) {
        mContextRefName = ref;
    }
    
    public Context getContext() {
        if(mContext != null) return mContext;
        if(mContextRefName != null) {
            mContext = (Context)getProject().getReference(mContextRefName);
            return mContext;
        }
        mContext = (Context)getProject().getReference(DEFAULT_CONTEXT_REF_NAME);
        
        if(mContext == null) {
           error("Context reference is not set.");
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
        AndroidDebugBridge adb = getAdb();
        
        for(IDevice device : adb.getDevices()) {
            if(device.getSerialNumber().equals(serialNo)) {
                return device;
            }
        }
        error("Device \"%s\" is not connected to ADB.", serialNo);
        return null;
    }
    
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
}
