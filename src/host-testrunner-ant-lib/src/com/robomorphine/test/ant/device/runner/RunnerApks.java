package com.robomorphine.test.ant.device.runner;

import com.robomorphine.test.ant.BaseTask;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RunnerApks {

    public static class Apk {
        
        private File mFile;
        public void setFile(File file) {
            mFile = file;            
        }
        
        public File getFile() {
            return mFile;
        }
        
        public void verify(BaseTask task) {
            if(mFile == null) {
                task.error("Attribute is required: \"file\"");
            }
            
            if(!mFile.exists()) {
                task.error("Specified apk file does not exist: %s", mFile.getAbsolutePath());
            }
            
            if(!mFile.isFile()) {
                task.error("Specified apk file is not a file: %s", mFile.getAbsolutePath());
            }
        }
    }
    
    private final BaseTask mTask;
    private Set<File> mApkFiles = new LinkedHashSet<File>();
    private File mTesterFile;
    
    public RunnerApks(BaseTask task) {
        mTask = task;
    }
    
    public File getTesterApk() {
        return mTesterFile;
    }
    
    public List<File> getApks() {
        return new LinkedList<File>(mApkFiles);
    }
    
    public void addConfiguredFileset(FileSet fileset) {
        Iterator<?> iter = fileset.iterator();
        while(iter.hasNext()) {
            FileResource res = (FileResource)iter.next();
            if(mApkFiles.add(res.getFile())) {
                mTask.info("Added apk:  %s", res.getFile().getName());
            }
        }
    }    
    
    public void addConfiguredApk(Apk apk) {
        apk.verify(mTask);
        if(mApkFiles.add(apk.getFile())) {
            mTask.info("Added apk:  %s", apk.getFile().getName());
        }
    }   
    
    public void addConfiguredTester(Apk apk) {
        if(mTesterFile != null) {
            mTask.error("Only single tester apk is allowed!");
        }
        mTesterFile = apk.getFile();
        addConfiguredApk(apk);
    }
}