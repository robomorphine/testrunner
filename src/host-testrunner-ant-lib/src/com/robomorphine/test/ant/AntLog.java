package com.robomorphine.test.ant;

import com.robomorphine.test.log.ILog;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;



public class AntLog implements ILog, BuildListener {
    
    private final Project mProject;
    private Task mCurrentTask;
    private Target mCurrentTarget;
    private boolean mVerbose;
    
    public AntLog(Project project) {
        mProject = project;
        mProject.addBuildListener(this);
    }
    
    public void setVerbose(boolean verbose) {
        mVerbose = verbose;
    }
    
    private void log(int level, Throwable ex, String format, Object...args) {
        String msg = String.format(format, args);
        if(mVerbose && level > Project.MSG_INFO) {
            level = Project.MSG_INFO;
        }
        if(mCurrentTask != null) {
            mCurrentTask.log(msg, ex, level);
        } else if(mCurrentTarget != null){
            mProject.log(mCurrentTarget, msg, ex, level);
        } else {
            mProject.log(msg, ex, level);
        }
    }
    
    @Override
    public void v(String format, Object... args) {
        log(Project.MSG_VERBOSE, null, format, args);
    }
    
    @Override
    public void i(String format, Object... args) {
        log(Project.MSG_INFO, null, format, args);
    }
    
    @Override
    public void w(String format, Object... args) {
        log(Project.MSG_WARN, null, format, args);
    }
    
    @Override
    public void e(Throwable ex, String format, Object... args) {
        log(Project.MSG_ERR, ex, format, args);
    }
    
    /* Build Listener */
    @Override
    public void buildStarted(BuildEvent event) {
    }
    
    @Override
    public void buildFinished(BuildEvent event) {
    }
    
    @Override
    public void messageLogged(BuildEvent event) {
    }
    
    @Override
    public void targetStarted(BuildEvent event) {
        mCurrentTarget = event.getTarget();
    }
    
    @Override
    public void targetFinished(BuildEvent event) {
        mCurrentTarget = null;
    }
    
    @Override
    public void taskStarted(BuildEvent event) {
        mCurrentTask = event.getTask();
    }
    
    @Override
    public void taskFinished(BuildEvent event) {
        mCurrentTask = null;
    }
}
