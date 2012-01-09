package com.robomorphine.test.ant.device;

import com.robomorphine.test.ant.BaseTask;
import com.robomorphine.test.emulator.RemoteLogcat;

import org.apache.tools.ant.BuildException;

public class StopLogcatTask extends BaseTask {

    private String mReferenceName;
    
    public void setRefId(String name) {
        mReferenceName = name;
    }
    
    
    @Override
    public void execute() throws BuildException {
        if(mReferenceName == null) {
            error("Logcat refernece name is not specified. Use \"logcatref\" attrobute");
        }
        
        RemoteLogcat logcat = (RemoteLogcat)getProject().getReference(mReferenceName);
        if(logcat == null) {
            warn("No logcat instance is found at reference %s", mReferenceName);
            return;
        }
        info("Stopping logcat %s", mReferenceName);
        logcat.stop();
    }
}
