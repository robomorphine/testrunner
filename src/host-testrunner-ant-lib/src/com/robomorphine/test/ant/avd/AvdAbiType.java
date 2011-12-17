package com.robomorphine.test.ant.avd;

import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISystemImage;

public class AvdAbiType {

    private final AvdConfigTask mAvdTask;
    private String mAbiTypeName;
    public AvdAbiType(AvdConfigTask task) {
        mAvdTask = task;
    }
    
    public void setType(String name) {
        IAndroidTarget target = mAvdTask.getResolvedTarget();
        while(target != null) {
            ISystemImage [] images = target.getSystemImages();
            for(ISystemImage image : images) {
                if(image.getAbiType().equals(name)) {
                    mAbiTypeName = name;
                    return;
                }
            }
            target = target.getParent();
        }
        mAvdTask.error("Specified abi type \"%s\" is invalid for target %s", 
                              name, mAvdTask.getTarget());
    }
    
    public String getType() {
        if(mAbiTypeName == null) {
            
            IAndroidTarget target = mAvdTask.getResolvedTarget();
            while(target != null) {
                ISystemImage [] images = target.getSystemImages();
                if(images != null && images.length > 0) {
                    mAbiTypeName = images[0].getAbiType();
                    break;
                }
                target = target.getParent();
            }
            
            if(mAbiTypeName == null) {
                mAvdTask.error("No system images are available for target: %s", 
                                mAvdTask.getTarget());
            }
        } 
        return mAbiTypeName;
    }
    
}
