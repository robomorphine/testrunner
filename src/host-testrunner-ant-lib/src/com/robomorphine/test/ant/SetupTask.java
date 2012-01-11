package com.robomorphine.test.ant;

import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.exception.AdbConnectionException;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;

import java.io.File;

public class SetupTask extends BaseTask implements BuildListener {
    
    private String mReferenceName = DEFAULT_CONTEXT_REF_NAME;
    private File mSdkDir;
    private boolean mForce = false;
    private boolean mLazy = true;
    private boolean mVerbose = false;
        
    public void setReference(String name) {
        mReferenceName = name;
    }
    
    public void setSdkDir(File dir) {
        mSdkDir = dir;
    }
    
    public void setForce(boolean force) {
        mForce = force;
    }
    
    public void setLazy(boolean lazy) {
        mLazy = lazy;
    }
    
    public void setVerbose(boolean verbose) {
        mVerbose = verbose;
    }
    
    public void setup() throws BuildException {
        Context context = (Context)getProject().getReference(DEFAULT_CONTEXT_REF_NAME);
        if(context != null && !mForce) {
            info("Setup was already done. Skipping it this time...");
            return;
        }
        
        if(mSdkDir == null) {
            error("Sdk directory was not set.");
        }
        if(!mSdkDir.exists()) {
            error("Sdk directory %s does not exist.", mSdkDir.getAbsolutePath());
        }
        
        AntLog log = new AntLog(getProject());
        log.setVerbose(mVerbose);
        
        context = new Context();
        
        try {
            TestManager manager = new TestManager(mSdkDir, log);
            if(!mLazy) {
                manager.connectAdb();
            } else {
                dbg("Skipping adb connection. Lazy setup is enabled.");
            }
            context.setTestManager(manager);
        } catch(AndroidLocationException ex) {
            error(ex, "Failed to create TestManager.");
        } catch(AdbConnectionException ex) {
            error(ex, "Failed to connect to adb.");
        }
        
        getProject().addReference(mReferenceName, context);
    }
    
    @Override
    public void execute() throws BuildException {
        
        if(getOwningTarget().getName().length() == 0) {
            getProject().addBuildListener(this);
            dbg("Detected the call from outside target. " + 
                "Deferring setup unitl first target is called.");
            return;
        }   
        setup();
    }
    
    @Override
    public void buildStarted(BuildEvent event) {
        //ignored
    }
    
    @Override
    public void buildFinished(BuildEvent event) {
        //ignored
    }
    
    @Override
    public void messageLogged(BuildEvent event) {
        //ignored
    }
    
    @Override
    public void targetStarted(BuildEvent event) {
        getProject().removeBuildListener(this);
        dbg("Starting deferred setup.");
        setup();
    }
    
    @Override
    public void targetFinished(BuildEvent event) {
        //ignored
    }
    
    @Override
    public void taskStarted(BuildEvent event) {
        //ignored
    }
    
    @Override
    public void taskFinished(BuildEvent event) {
        //ignored
    }
}
