package com.robomorphine.test.ant;

import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISystemImage;

public class AvdAbiType {

    private final CreateAvdTask mCreateAvdTask;
    private String mAbiTypeName;
    public AvdAbiType(CreateAvdTask task) {
        mCreateAvdTask = task;
    }
    
    public void setType(String name) {
        IAndroidTarget target = mCreateAvdTask.getResolvedTarget();
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
        mCreateAvdTask.error("Specified abi type \"%s\" is invalid for target %s", 
                              name, mCreateAvdTask.getTarget());
    }
    
    public String getType() {
        if(mAbiTypeName == null) {
            
            IAndroidTarget target = mCreateAvdTask.getResolvedTarget();
            while(target != null) {
                ISystemImage [] images = target.getSystemImages();
                if(images != null && images.length > 0) {
                    mAbiTypeName = images[0].getAbiType();
                    break;
                }
                target = target.getParent();
            }
            
            if(mAbiTypeName == null) {
                mCreateAvdTask.error("No system images are available for target: %s", 
                                      mCreateAvdTask.getTarget());
            }
        } 
        return mAbiTypeName;
    }
    
}
