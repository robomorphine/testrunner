package com.robomorphine.test.sdktool;

import com.robomorphine.test.log.ILog;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class ToolsManager {

    private final File mSdkPath;
    private final ILog mLogger;
    private final ConcurrentHashMap<Process, Boolean> mRunningProcesses;
    
    public ToolsManager(File sdkPath, ILog log) {
        mSdkPath = sdkPath;
        mLogger = log;         
        mRunningProcesses = new ConcurrentHashMap<Process, Boolean>();
    }
    
    protected ILog getLogger() {
        return mLogger;
    }
    
    protected void onProcessStarted(Process process) {
        mRunningProcesses.put(process, true);
    }
    
    protected void onProcessCompleted(Process process) {
        mRunningProcesses.remove(process);
    }
    
    public void terminate() {
        LinkedList<Process> processes = new LinkedList<Process>(mRunningProcesses.keySet());
        for(Process process : processes) {  
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
