package com.robomorphine.test.ant;



import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.robomorphine.test.TestManager;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;

public class BaseTask extends Task {
    
    protected static final String DEFAULT_CONTEXT_REF_NAME = "rbm-context";
     
    private Reference mContextRef;
    private Context mContext;
    
    public void setContextRef(Reference ref) {
        mContextRef = ref;
    }
    
    public Context getContext() {
        if(mContext != null) return mContext;
        if(mContextRef != null) {
            mContext = (Context)mContextRef.getReferencedObject(getProject());
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
    
    public String getDeviceSerialNumber() {
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
    
    protected void error(String format, Object...args) {
        error(null, format, args);        
    }
    
    protected void error(Throwable ex, String format, Object...args) {
        String msg = String.format(format, args);
        throw new BuildException(msg, ex, getLocation());
    }
    
}
