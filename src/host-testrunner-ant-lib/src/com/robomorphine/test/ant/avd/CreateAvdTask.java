package com.robomorphine.test.ant.avd;


import org.apache.tools.ant.BuildException;

public class CreateAvdTask extends AvdConfigTask {
        
    private String mAvdConfigId;
    private String mName;
    private boolean mRemovePrevious = false;
    
    public void setAvdConfig(String avdId) {
        mAvdConfigId = avdId;
    }
        
    public void setName(String name) {
        mName = name;
    }
    
    public String getName() {
        if(mName == null) {
            error("AVD name was not set.");
        }
        return mName;
    }
    
    public void setForce(boolean force) {
        mRemovePrevious = true;
    }
     
    @Override
    public void execute() throws BuildException {
        AvdConfigTask avd = this;
        if(mAvdConfigId != null) {
            avd = (AvdConfigTask)getProject().getReference(mAvdConfigId);
        }
        avd.create(getName(), mRemovePrevious);
    }
}
