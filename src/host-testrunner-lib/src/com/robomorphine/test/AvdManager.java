package com.robomorphine.test;

import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.avd.AvdInfo;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.ISdkLog2ILog;

import java.io.File;
import java.util.HashMap;

public class AvdManager {
    
    private final SdkManager mSdkManager;
    private final com.android.sdklib.internal.avd.AvdManager mSdkAvdManager;
    private final ISdkLog2ILog mSdkLog;
    
    public AvdManager(SdkManager manager, ILog log) throws AndroidLocationException {
        mSdkManager = manager;
        mSdkLog = new ISdkLog2ILog(log);
        mSdkAvdManager = new com.android.sdklib.internal.avd.AvdManager(mSdkManager, mSdkLog);
    }
    
    public AvdInfo createAvd(String avdName, String targetId, int sdcardSizeMb,
            HashMap<String, String> hardwareConfig, boolean enableSnapshot, boolean removePrevious)
            throws AndroidLocationException {
        
        File avdPath = AvdInfo.getDefaultAvdFolder(mSdkAvdManager, avdName);
        IAndroidTarget target = mSdkManager.getTargetFromHashString(targetId);        
        if(target == null) {
            throw new IllegalArgumentException("Failed to resolve target: " + targetId);
        }
        
        String abiType = null;
        ISystemImage [] images = target.getSystemImages();
        if(images == null || images.length < 1) {
            throw new IllegalArgumentException("No system images are available for target: " + targetId);
        }
        abiType = images[0].getAbiType();
        
        String sdcardSize = null;
        if(sdcardSizeMb > 0) {
            sdcardSize = String.format("%dM", sdcardSizeMb);
        }
                 
        AvdInfo info = mSdkAvdManager.createAvd(avdPath, avdName, 
                                                target, abiType, null, sdcardSize,
                                                hardwareConfig, enableSnapshot, 
                                                removePrevious, false,
                                                mSdkLog);
        
        return info;
    }

    public boolean deleteAvd(String avdName) {
        for(AvdInfo avdInfo : mSdkAvdManager.getAllAvds()) {
            if(avdInfo.getName().equals(avdName)) {
                return mSdkAvdManager.deleteAvd(avdInfo, mSdkLog);
            }
        }
        return false;
    }    
}
