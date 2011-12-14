package com.robomorphine.test.sdktool;

import com.robomorphine.test.log.ILog;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class ToolsManager {

    private final File mSdkPath;
    private final ILog mLogger;
    private ConcurrentHashMap<Process, Boolean> mRunningProcesses = new ConcurrentHashMap<Process, Boolean>();
    
    public ToolsManager(File sdkPath, ILog log) {
        mSdkPath = sdkPath;
        mLogger = log;         
    }
    
    ILog getLogger() {
        return mLogger;
    }
    
    void onProcessStarted(Process process) {
        mRunningProcesses.put(process, true);
    }
    
    void onProcessCompleted(Process process) {
        mRunningProcesses.remove(process);
    }
    
    public void terminate() {
        for(Process process : new LinkedList<Process>(mRunningProcesses.keySet())) {
            process.destroy();
            mRunningProcesses.remove(process);
        }
    }
    
    public SdkTool createTool(File absolutExePath) {
        return new SdkTool(absolutExePath, this);
    }
    
    public AaptTool createAaptTool() {
        return new AaptTool(mSdkPath, this);
    }
    
    public AdbTool createAdbTool() {
        return new AdbTool(mSdkPath, this);
    }
    
    public EmulatorTool createEmulatorTool() {
        return new EmulatorTool(mSdkPath, this);
    }
}
