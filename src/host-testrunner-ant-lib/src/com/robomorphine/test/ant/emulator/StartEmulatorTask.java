package com.robomorphine.test.ant.emulator;

import com.robomorphine.test.TestManager;
import com.robomorphine.test.ant.BaseTask;
import com.robomorphine.test.emulator.EmulatorStarter;

import org.apache.tools.ant.BuildException;

import java.util.LinkedList;
import java.util.List;

public class StartEmulatorTask extends BaseTask {

    public static class Arg {
        private String mValue;
        public void setValue(String value) {
            mValue = value;
        }
        
        public String getValue() {
            return mValue;
        }
    }
    
    private String mAvdName;
    private String mSerialNumberPropertyName;
    private final List<String> mEmulatorArgs = new LinkedList<String>();
    private long mConnectTimeout = EmulatorStarter.DEFAULT_CONNECT_TIMEOUT;
    private long mBootTimeout = EmulatorStarter.DEFAULT_BOOT_TIMEOUT;
    private long mLowCpuTimeout = EmulatorStarter.DEFAULT_LOW_CPU_TIMEOUT;
    private int mLowCpuThreshold = EmulatorStarter.DEFAULT_LOW_CPU_THRESHOLD;
    private int mAttempts = 3;
    
    public void setAvd(String name) {
        mAvdName = name;
    }
    
    public void setSerialProperty(String propertyName) {
        mSerialNumberPropertyName = propertyName;
    }
    
    public void setConnectTimeout(long timeout) {
        mConnectTimeout = timeout;
    }
    
    public void setBootTimeout(long timeout) {
        mBootTimeout = timeout;
    }
    
    public void setLowCpuTimeout(long timeout) {
        mLowCpuTimeout = timeout;
    }
    
    public void setLowCpuThreshold(int threshold) {
        mLowCpuThreshold = threshold;
    }
    
    public void setAttempts(int count) {
        mAttempts = count;
    }
    
    public void addConfiguredArg(Arg arg) {
        if(arg.getValue() == null) {
            error("Emulator argument has no value.");
        }
        mEmulatorArgs.add(arg.getValue());
    }
    
    @Override
    public void execute() throws BuildException {
        if(mAvdName == null) {
            error("Avd name is not specified.");
        }
        
        if(mSerialNumberPropertyName == null) {
            error("Property name that should contain serial number of " + 
                  " started emulator is not specified. Use \"serialProperty\" attribute.");
        }
        
        TestManager testManager = getTestManager();
        EmulatorStarter starter = new EmulatorStarter(testManager);
        starter.setBootTimeout(mBootTimeout);
        starter.setConnectTimeout(mConnectTimeout);
        starter.setLowCpuTimeout(mLowCpuTimeout);
        starter.setLowCpuThreshold(mLowCpuThreshold);
        info("Starting emulator - %s.", mAvdName);
        try {
            String serialNo = starter.start(mAttempts, mAvdName, mEmulatorArgs);
            getProject().setProperty(mSerialNumberPropertyName, serialNo);
            info("Emulator successfuly started: \"%s\"", serialNo);
        } catch(Exception ex) {
            error(ex, "Failed to start emulator.");
        }
    };
}
