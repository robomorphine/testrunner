package com.robomorphine.test.emulator;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.robomorphine.test.exception.DeviceNotConnectedException;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.PrefixedLog;

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
    
    private final Object mSync = new Object();
    private boolean mRunning = false;
    private ShellOutputThread mOutputThread;
    private ShellOutputReceiver mOutputReceiver;
    private OutputStream mOutputStream;
    
    private final List<String> mArgs = new LinkedList<String>();
    
    public RemoteLogcat(AndroidDebugBridge adb, String serialNo, File file, ILog log) {
        mAdb = adb;
        mSerialNo = serialNo;
        mLog = new PrefixedLog(RemoteLogcat.class.getSimpleName(), log);
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
            throw new IllegalStateException("Can't add arguments while running.");
        }
        mArgs.add(arg);
    }
    
    public void start(boolean async) throws DeviceNotConnectedException, IOException {
        synchronized (mSync) {
            if(mRunning) {
                mLog.w("Logcat is already running. Ignored.");
                return;
            }
        }
        
        IDevice device = getDevice();
        mOutputStream = new FileOutputStream(mOutputFile);
        mOutputReceiver = new ShellOutputReceiver();
        mOutputThread = new ShellOutputThread(device);
        
        synchronized (mSync) {
            mRunning = true;
        }
        
        if(async) {
            mOutputThread.start();
        } else {
            mOutputThread.run(false);
        }
    }
    
    public void stop() {
        synchronized (mSync) {
            if(!mRunning) {
                mLog.w("Logcat wasn't running. Ignored.");
                return;
            }
            mRunning = false;
        }
        try {
            mOutputStream.flush();
        } catch(IOException ex) {  // NOPMD 
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
            super();
            mDevice = device;
        }
        
        @Override
        public void run() {
            run(true);
        }
        
        public void run(boolean async) {
            mLog.v("Logcat started.");
            try {
                mDevice.executeShellCommand(getCmd(), mOutputReceiver);
            } catch(Exception ex) {
                mLog.e(ex, "Failed to start logcat.");
            }
            
            try {
                mOutputStream.close();
            } catch(IOException ex) {  // NOPMD 
                //ignore
            }
            
            if(isRunning()) {
                if(async) {
                    mLog.w("Logcat has prematurely stopped.");
                }
                synchronized (mSync) {
                    mRunning = false;
                }
            } else {
                mLog.v("Logcat has stopped.");
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
                mLog.e(ex, "Failed to write logcat data to file.");
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
                mLog.e(ex, "Failed to flush.");
            }
        }
        
        @Override
        public boolean isCancelled() {
            return !isRunning();
        }
    }
}
