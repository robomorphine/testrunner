package com.robomorphine.test.ant;

import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.robomorphine.test.AdbConnectionException;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.log.StdLog;

import org.apache.tools.ant.BuildException;

import java.io.File;

public class SetupTask extends BaseTask {
    
    private String mReferenceName = DEFAULT_CONTEXT_REF_NAME;
    private File mSdkDir;
    
    public void setReference(String name) {
        mReferenceName = name;
    }
    
    public void setSdkDir(File dir) {
        mSdkDir = dir;
    }
    
    @Override
    public void execute() throws BuildException {
        if(mSdkDir == null) {
            error("Sdk directory was not set.");
        }
        if(!mSdkDir.exists()) {
            error("Sdk directory %s does not exist.", mSdkDir.getAbsolutePath());
        }
        
        Context context = new Context();
        try {
            TestManager manager = new TestManager(mSdkDir, new StdLog());
            manager.connectAdb();
            context.setTestManager(manager);
        } catch(AndroidLocationException ex) {
            error(ex, "Failed to create TestManager.");
        } catch(AdbConnectionException ex) {
            error(ex, "Failed to connect to adb.");
        }
        
        getProject().addReference(mReferenceName, context);
    }
}
