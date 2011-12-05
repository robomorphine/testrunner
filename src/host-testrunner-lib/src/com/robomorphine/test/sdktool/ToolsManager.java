package com.robomorphine.test.sdktool;

import com.robomorphine.test.log.ILog;

import org.python.core.util.ConcurrentHashSet;

import java.io.File;
import java.util.LinkedList;
import java.util.Set;

public class ToolsManager {

    private final File mSdkPath;
    private final ILog mLogger;
    private Set<Process> mRunningProcesses = new ConcurrentHashSet<Process>();
    
    public ToolsManager(File sdkPath, ILog log) {
        mSdkPath = sdkPath;
        mLogger = log;         
    }
    
    ILog getLogger() {
        return mLogger;
    }
    
    void onProcessStarted(Process process) {
        mRunningProcesses.add(process);
    }
    
    void onProcessCompleted(Process process) {
        mRunningProcesses.remove(process);
    }
    
    public void terminate() {
        for(Process process : new LinkedList<Process>(mRunningProcesses)) {
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
