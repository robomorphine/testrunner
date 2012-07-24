package com.robomorphine.test.ant.device.runner;

import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.robomorphine.test.ApkManager;
import com.robomorphine.test.ApkManager.ReasonInstallException;
import com.robomorphine.test.ant.BaseTask;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.sdktool.AaptTool;

import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class RunTestsTask extends BaseTask { //NOPMD

    private static final String DEFAULT_RUNNER_NAME = "android.test.InstrumentationTestRunner";
    
    public static class JUnit {
        private boolean mMultiple = true;
        private File mDir;
        
        public void setMultiple(boolean multiple) {
            mMultiple = multiple;
        }
        
        public boolean isMultiple() {
            return mMultiple;
        }
        
        public void setDir(File file) {
            mDir = file;
        }
        
        public File getDir() {
            return mDir;
        }
    }
        
    private String mPackageName;
    private String mRunnerName = DEFAULT_RUNNER_NAME;
    private final  RunnerArgs mArgs = new RunnerArgs(this);
    private final RunnerApks mApks = new RunnerApks(this);
    private JUnitTestRunListener mJUnitListener;
    private boolean mUninstall = true;
    private boolean mFailOnError = false;
    private boolean mSuccessOnOlderSdk = true;
    
    public void setPackage(String name) {
        mPackageName = name;
    }
    
    public void setRunner(String name) {
        mRunnerName = name;        
    }
    
    public void setFailOnError(boolean fail) {
        mFailOnError = fail;
    }
    
    public void setUninstall(boolean uninstall) {
        mUninstall = uninstall;
    }
    
    public void setSuccessOnOlderSdk(boolean successOnOlderSdk) {
        mSuccessOnOlderSdk = successOnOlderSdk;
    }
    
    public RunnerArgs createArgs() {              
        return mArgs;
    }
    
    public RunnerApks createApks() {
        return mApks;
    }
    
    public void addConfiguredJunit(JUnit junit) {
        if(mJUnitListener != null) {
            error("Only one junit element can be specified.");
        }
              
        if(junit.getDir() == null) {
            error("Missing junit attribute: \"dir\".");
         }
        
        mJUnitListener = new JUnitTestRunListener(junit.getDir(), junit.isMultiple());
    }
    
    private boolean installApks(List<File> apks, boolean reinstall) {
        ApkManager apkManager = getTestManager().getApkManager();
        for(File apk : apks) {
            try {
                apkManager.install(getDevice(), apk, reinstall);
            } catch(ReasonInstallException ex) {
                String reason = ex.getReason();
                if (mSuccessOnOlderSdk && ApkManager.INSTALL_FAILED_OLDER_SDK.equals(reason)) {
                    //so we tried to install apks, but detected that current emulator or 
                    //device has SDK version that is too old for one of the apks. 
                    //We're done.
                    ILog log = getTestManager().getLogger();
                    log.w("Wasn't able to install apk because current device has too old API level.");
                    log.w("(If you want to fail build in such cases use successOnOlderSdk=\"false\".)");
                    return false;                    
                } 
                error(ex, "Failed to install apk: %s", apk.getAbsolutePath());
            }  catch(Exception ex) {
                error(ex, "Failed to install apk: %s", apk.getAbsolutePath());
            } 
        }       
        return true;
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
    public void execute() throws BuildException {  //NOPMD
        
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
        if(mUninstall) {
            uninstallApks(apks, false);
        }
        
        boolean installed = installApks(apks, !mUninstall);/* if not uninstalled, use reinstall */ 
        if (!installed) {
            ILog log = getTestManager().getLogger();
            log.w("Test run is marked successful without running tests.");
            /* Failed to install apks, but this was non-critical failure. 
             * So we assume success. This is more of a hackaround. 
             * See more details inside installApks(). */ 
            uninstallApks(apks, false);
            return;
        }
        
        DefaultTestRunListener listener = new DefaultTestRunListener(this);
        List<ITestRunListener> listeners = new LinkedList<ITestRunListener>();
        listeners.add(listener);
        if(mJUnitListener != null) {
            listeners.add(mJUnitListener);
        }
        
        RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(mPackageName, mRunnerName, getDevice());
        for(Entry<String,String> arg : mArgs.getArgs().entrySet()) {
            runner.addInstrumentationArg(arg.getKey(), arg.getValue());
        }
        
        info("Started tests from package \"%s\" on device \"%s\"", mPackageName, getDevice().getSerialNumber());        
        try {
            runner.run(listeners);
        } catch(Exception ex) {
            error(ex, "Failed to run tests in package \"%s\" using runner \"%s\". Error: %s",
                       mPackageName, mRunnerName, ex.getMessage());
        }
        info("Finished tests from package \"%s\" on device \"%s\"", mPackageName, getDevice().getSerialNumber());
                
        if(listener.getFailedTestCount() > 0 && mFailOnError) {
            error("Failing because %d tests failed.", listener.getFailedTestCount());
        }
        
        if(mUninstall) {
            uninstallApks(apks, false);
        }
    }
}
