package com.robomorphine.test.ant;

import com.robomorphine.test.TestManager;
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
    private List<String> mEmulatorArgs = new LinkedList<String>();
    private long mConnectTimeout = EmulatorStarter.DEFAULT_CONNECT_TIMEOUT;
    private long mBootTimeout = EmulatorStarter.DEFAULT_BOOT_TIMEOUT;
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
        EmulatorStarter starter = new EmulatorStarter(testManager, mConnectTimeout, mBootTimeout);
        try {
            String serialNo = starter.start(mAttempts, mAvdName, mEmulatorArgs);
            getProject().setProperty(mSerialNumberPropertyName, serialNo);
        } catch(Throwable ex) {
            error(ex, "Failed to start emulator.");
        }
    };
}
