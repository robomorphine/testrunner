package com.robomorphine.test.ant;

import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.robomorphine.test.AdbConnectionException;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.log.StdLog;

import org.apache.tools.ant.BuildException;

import java.io.File;

public class SetupTask extends BaseTask {
    
    private static String SETUP_DONE_REF_NAME = "rbm-setup-done";
    
    private String mReferenceName = DEFAULT_CONTEXT_REF_NAME;
    private File mSdkDir;
    private boolean mForce = false;
    
    public void setReference(String name) {
        mReferenceName = name;
    }
    
    public void setSdkDir(File dir) {
        mSdkDir = dir;
    }
    
    public void setForce(boolean force) {
        mForce = force;
    }
    
    @Override
    public void execute() throws BuildException {
        Boolean setupDone = (Boolean)getProject().getReference(SETUP_DONE_REF_NAME);
        if(setupDone!=null && setupDone && !mForce) {
            info("Setup was already done. Skipping it this time...");
            return;
        }
        
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
        getProject().addReference(SETUP_DONE_REF_NAME, true);
    }
}
