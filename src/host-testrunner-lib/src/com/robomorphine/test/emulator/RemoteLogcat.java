package com.robomorphine.test.emulator;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.PrefixedLog;
import com.sun.xml.internal.txw2.IllegalSignatureException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class RemoteLogcat {
    
    private final AndroidDebugBridge mAdb;
    private final String mSerialNo;
    private final ILog mLog;
    private final File mOutputFile;
    
    private Object mSync = new Object();
    private boolean mRunning = false;
    private ShellOutputThread mOutputThread;
    private ShellOutputReceiver mOutputReceiver;
    private OutputStream mOutputStream;
    
    private final List<String> mArgs = new LinkedList<String>();
    
    public RemoteLogcat(AndroidDebugBridge adb, String serialNo, File file, ILog log) {
        mAdb = adb;
        mSerialNo = serialNo;
        mLog = new PrefixedLog("Logcat", log);
        mOutputFile = file;
    }
    
    private IDevice getDevice() throws DeviceNotConnectedException {
        for(IDevice device : mAdb.getDevices()) {
            if(device.getSerialNumber().equals(mSerialNo)) {
                return device;
            }
        }
        throw new DeviceNotConnectedException("Device " + mSerialNo + " is not connected.");
    }
    
    public void addArg(String arg) {
        if(isRunning()) {
            throw new IllegalSignatureException("Can't add arguments while running.");
        }
        mArgs.add(arg);
    }
    
    public void start(boolean async) throws DeviceNotConnectedException, IOException {
        mLog.info("Starting logcat...");
        synchronized (mSync) {
            if(mRunning) {
                mLog.warning("Logcat is already running. Ignored.");
                return;
            }
            mRunning = true;
        }
        
        IDevice device = getDevice();
        mOutputStream = new FileOutputStream(mOutputFile);
        mOutputReceiver = new ShellOutputReceiver();
        mOutputThread = new ShellOutputThread(device);
        
        if(async) {
            mOutputThread.start();
        } else {
            mOutputThread.run();
        }
    }
    
    public void stop() {
        mLog.info("Stopping logcat...");
        synchronized (mSync) {
            if(!mRunning) {
                mLog.warning("Logcat wasn't running. Ignored.");
                return;
            }
            mRunning = false;
        }
        try {
            mOutputStream.flush();
        } catch(IOException ex) {
            //ignore
        }
        mOutputThread.interrupt();
    }
    
    public boolean isRunning() {
        return mRunning;
    }
    
    protected String getCmd() {
        StringBuilder builder = new StringBuilder();
        builder.append("logcat");
        for(String arg : mArgs) {
            builder.append(" ");
            builder.append(arg);
        }
        return builder.toString();
    }
    
    private class ShellOutputThread extends Thread {                
        private final IDevice mDevice;        
        public ShellOutputThread(IDevice device) {
            mDevice = device;
        }
        
        @Override
        public void run() {
            mLog.info("Logcat started.");
            try {
                mDevice.executeShellCommand(getCmd(), mOutputReceiver);
            } catch(Exception ex) {
                mLog.error(ex, "Failed to start logcat.");
            }
            
            try {
                mOutputStream.close();
            } catch(IOException ex) {
                //ignore
            }
            
            if(isRunning()) {
                mLog.warning("Logcat has prematurely stopped.");
                synchronized (mSync) {
                    mRunning = false;
                }
            } else {
                mLog.info("Logcat has stopped.");
            }
        }
    }
    
    private class ShellOutputReceiver implements IShellOutputReceiver {
        @Override
        public void addOutput(byte[] buffer, int offset, int len) {
            try {
                if(!isRunning()) {
                    return;
                }
                mOutputStream.write(buffer, offset, len);
            } catch(IOException ex) {
                mLog.error(ex, "Failed to write logcat data to file.");
            }
        }
        
        @Override
        public void flush() {
            try {
                if(!isRunning()) {
                    return;
                }                
                mOutputStream.flush();
            } catch(IOException ex) {
                mLog.error(ex, "Failed to flush.");
            }
        }
        
        @Override
        public boolean isCancelled() {
            return !isRunning();
        }
    }
}
