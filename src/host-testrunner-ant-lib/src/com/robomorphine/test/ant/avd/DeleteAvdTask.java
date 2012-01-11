package com.robomorphine.test.ant.avd;

import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.internal.avd.AvdManager;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.ant.BaseTask;

import org.apache.tools.ant.BuildException;

public class DeleteAvdTask extends BaseTask {
    
    private String mName;
    
    public void setName(String name) { 
        mName = name;
    }
    
    private AvdInfo findAvdInfo(String name) {
        AvdManager avdManager = getTestManager().getAvdManager();
        for(AvdInfo info : avdManager.getAllAvds()) {
            if(info.getName().equals(name)) {
                return info;
            }
        }        
        return null;
    }
    
    @Override
    public void execute() throws BuildException {        
        if(mName == null) {
            error("Avd name is not specified.");
        }
        
        TestManager testManager = getTestManager();
        AvdManager avdManager = getTestManager().getAvdManager();
        AvdInfo avdInfo = findAvdInfo(mName);
        
        if(avdInfo == null) {
            warn("Avd \"%s\" was not found. Assuming success.", mName);
        } else {
            avdManager.deleteAvd(avdInfo, testManager.getSdkLogger());
            avdInfo = findAvdInfo(mName);
            if(avdInfo == null) {
                info("Avd %s was successfully deleted.", mName);
            } else {
                error("Failed to delete \"%s\" avd.", mName);
            } 
        }
    }
    
}
