import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class engine extends Task implements BuildListener {
    
    @Override
    public void log(String msg) {
        getProject().log(msg);
    }
    
    @Override
    public void log(String msg, int msgLevel) {
        getProject().log(msg, msgLevel);
    }
    
    @Override
    public void log(String msg, Throwable t, int msgLevel) {
        getProject().log(msg, t, msgLevel);
    }
    
    @Override
    public void log(Throwable t, int msgLevel) {
        getProject().log(this, t.getMessage(), t, msgLevel);
    }
    
    @SuppressWarnings("unchecked")
    @Override    
    public void execute() throws BuildException {
        File buildfile = new File(getProject().getProperty("ant.file"));
        log("-------------------------------------------");
        log(" Running tests in: " + buildfile.getName());
        log("-------------------------------------------");
        log("");        
        
        List<String> targets = new ArrayList<String>(getProject().getTargets().keySet());
        Collections.sort(targets);
                        
        log("Enumerating targets.", Project.MSG_INFO);
        
        Vector<String> beforeTargets = new Vector<String>();
        for(String target: targets) {
            if(target.startsWith("before")) {
                log("before: " + target, Project.MSG_VERBOSE);
                beforeTargets.add(target);
            }
        }
        log("\"Before\" targets: " + beforeTargets.size(), Project.MSG_INFO);
        
        Vector<String> testTargets = new Vector<String>();
        for(String target: targets) {
            if(target.startsWith("test")) {
                log("test: " + target, Project.MSG_VERBOSE);
                testTargets.add(target);
            }
        }
        log("\"Test\" targets: " + testTargets.size(), Project.MSG_INFO);
        
        Vector<String> afterTargets = new Vector<String>();
        for(String target: targets) {
            if(target.startsWith("after")) {
                log("after: " + target, Project.MSG_VERBOSE);
                afterTargets.add(target);
            }
        }
        log("\"After\" targets: " + afterTargets.size(), Project.MSG_INFO);
        
        log("");
        log("Executing targets.", Project.MSG_INFO);
        log("");
        
        
        getProject().executeTargets(beforeTargets);
        
        getProject().addBuildListener(this);
        try {
            for(String targetName : testTargets) { 
                long before = System.currentTimeMillis();
                getProject().executeTarget(targetName);   
                long elapsed = System.currentTimeMillis() - before;
                log("SUCCESS: elapsed " + elapsed + " ms");
            }
        } catch(Exception ex) {
            log("---------------------", Project.MSG_ERR);
            log(ex.getMessage(), Project.MSG_ERR);
            log("FAILED", Project.MSG_ERR);
            log("---------------------", Project.MSG_ERR);
            throw new BuildException(ex);
        } finally {
            getProject().removeBuildListener(this);
            getProject().executeTargets(afterTargets);
        }
    }
    
    @Override
    public void targetStarted(BuildEvent event) {
        Target target = event.getTarget(); 
        String info = target.getDescription();
        if(info != null) {
            log("---------------------------");
            log("Test info: " + info);
            log("---------------------------");
        }
    }
    
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
    public void targetFinished(BuildEvent event) {
    }
    
    
    
    @Override
    public void taskFinished(BuildEvent event) {
    }
    
    @Override
    public void taskStarted(BuildEvent event) {        
    }
}
