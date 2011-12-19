package com.robomorphine.test.ant.device.runner;

import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.robomorphine.test.ApkManager;
import com.robomorphine.test.ant.BaseTask;
import com.robomorphine.test.sdktool.AaptTool;

import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

public class RunTestsTask extends BaseTask {

    private static final String DEFAULT_RUNNER_NAME = "android.test.InstrumentationTestRunner";
    
    private String mPackageName;
    private String mRunnerName = DEFAULT_RUNNER_NAME;
    private RunnerArgs mArgs = new RunnerArgs(this);
    private RunnerApks mApks = new RunnerApks(this);
    
    private boolean mFailOnError = false;
    
    
    public void setPackage(String name) {
        mPackageName = name;
    }
    
    public void setRunner(String name) {
        mRunnerName = name;        
    }
    
    public void setFailOnError(boolean fail) {
        mFailOnError = fail;
    }
    
    public RunnerArgs createArgs() {
        return mArgs;
    }
    
    public RunnerApks createApks() {
        return mApks;
    }
    
    private void installApks(List<File> apks) {
        ApkManager apkManager = getTestManager().getApkManager();
        for(File apk : apks) {
            try {
                apkManager.install(getDevice(), apk, false);
            } catch(Exception ex) {
                error(ex, "Failed to install apk: %s", apk.getAbsolutePath());
            }
        }       
    }
    
    private void uninstallApks(List<File> apks, boolean failOnError) {
        ApkManager apkManager = getTestManager().getApkManager();
        for(File apk : apks) {
            try {
                apkManager.uninstall(getDevice(), apk);
            } catch(Exception ex) {
                if(failOnError) {
                    error(ex, "Failed to uninstall apk: %s", apk.getAbsolutePath());
                } else {
                    warn("Failed to uninstall apk: %s", apk.getAbsolutePath());
                }
            }
        }
    }
    
    @Override
    public void execute() throws BuildException {
        
        if(mPackageName == null) {
            warn("Tester package is not specified. Trying to extract package name from tester apk.");
            
            File testerApk = mApks.getTesterApk() ;
            if(testerApk != null) {
                AaptTool aapt = getTestManager().getToolsManager().createAaptTool();
                try {
                    mPackageName = aapt.getPacakgeName(testerApk);
                } catch(IOException ex) {
                    error(ex, "Failed to extract tester apk's package name.");
                }
            } else {
                error("Nor package name neither tester apk are specified. Can not run tests.");
            }
        }
        
        List<File> apks = mApks.getApks();
        uninstallApks(apks, false);
        installApks(apks);        
        
        DefaultTestRunListener listener = new DefaultTestRunListener(this);
        RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(mPackageName, mRunnerName, getDevice());
        
        for(Entry<String,String> arg : mArgs.getArgs().entrySet()) {
            runner.addInstrumentationArg(arg.getKey(), arg.getValue());
        }
        
        info("Started tests from package \"%s\" on device \"%s\"", mPackageName, getDevice().getSerialNumber());        
        try {
            runner.run(listener);
        } catch(Exception ex) {
            error(ex, "Failed to run tests in package \"%s\" using runner \"%s\".", mPackageName, mRunnerName);
        }
        info("Finished tests from package \"%s\" on device \"%s\"", mPackageName, getDevice().getSerialNumber());
                
        if(listener.getFailedTestCount() > 0 && mFailOnError) {
            error("Failing because %d tests failed.", listener.getFailedTestCount());
        }
        
        uninstallApks(apks, false);
        
    }
}
