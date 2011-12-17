package com.robomorphine.test.ant;

import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.internal.avd.AvdManager;

import org.apache.tools.ant.BuildException;

import java.io.File;
import java.util.HashMap;

public class CreateAvdTask extends BaseTask {
    
    private String mName;
    private String mTarget;
    private IAndroidTarget mResolvedTarget;
    
    private HashMap<String, String> mHardwareConfig = new HashMap<String, String>();
    private boolean mEnableSnapshot = false;
    private boolean mRemovePrevious = false;    
         
    private AvdAbiType mAbiType = null;
    private AvdSnapshot mSnapshot = null;
    private AvdSdcard mSdcard = null;
    private AvdScreen mScreen = null;
    private AvdHardware mHardware = null;
    
        
    public void setName(String name) {
        mName = name;
    }
    
    public String getName() {
        if(mName == null) {
            error("AVD name was not set.");
        }
        return mName;
    }
    
    public void setTarget(String target) {
        mTarget = target;
    }
    
    public String getTarget() {
        if(mTarget == null) {
            error("AVD target was not set.");
        }
        return mTarget;
    }
    
    public IAndroidTarget getResolvedTarget() {
        if(mResolvedTarget != null) {
            return mResolvedTarget;
        }
        
        SdkManager sdkManager = getSdkManager();
        String target = getTarget();
        IAndroidTarget resolvedTarget = sdkManager.getTargetFromHashString(target);
        if(resolvedTarget == null) {
            error("Failed to resolve \"%s\" target.", target);
        }
        mResolvedTarget = resolvedTarget;
        return resolvedTarget;
    }
    
    public void setForce(boolean force) {
        mRemovePrevious = force;
    }
    
    public AvdAbiType createAbi(){
        if(mAbiType != null) {
            error("Only one abi is allowed.");
        }
        mAbiType = new AvdAbiType(this);
        return mAbiType;
    }
    
    public AvdSnapshot createSnapshot() {
        if(mSnapshot != null) {
            error("Only one snapshot is allowed.");
        }
        mSnapshot = new AvdSnapshot();
        return mSnapshot;
    }
    
    public AvdSdcard createSdcard() {
        if(mSdcard != null) {
            error("Only one sdcard is allowed.");
        }
        mSdcard = new AvdSdcard(this);
        return mSdcard;
    }
    
    public AvdScreen createScreen() {
        if(mScreen != null) {
            error("Only one screen is allowed.");
        }
        mScreen = new AvdScreen(this);        
        return mScreen;
    }
    
    public AvdHardware createHardware() {
        if(mHardware != null) {
            error("Only one hardware is allowed.");
        }
        mHardware = new AvdHardware(this, mHardwareConfig);
        return mHardware;
    }
    
    @Override
    public void execute() throws BuildException {
        AvdManager avdManager = getTestManager().getAvdManager();
        IAndroidTarget target = getResolvedTarget();
        
        /* 1. AVD path  - where created avd will be placed */
        File avdPath = null;
        try {
            avdPath = AvdInfo.getDefaultAvdFolder(avdManager, getName());
        } catch(AndroidLocationException ex) {
            error(ex, "Failed to determine path for new avd.");
        }
        
        /* 2. ABI type */
        if(mAbiType == null) {
            mAbiType = new AvdAbiType(this);
        }
        String abiType = mAbiType.getType();

        /* 3. snapshot */
        mEnableSnapshot = false;
        if(mSnapshot != null) {
            mEnableSnapshot = mSnapshot.isEnabled();
        }
        
        /* 4. resolution & density*/
        String skin = null;
        if(mScreen != null) {
            skin = mScreen.getResolution();
            if(mScreen.hasDensity()) {
                mHardwareConfig.put("hw.lcd.density", Integer.toString(mScreen.getDensity()));
            }
        }
        
        /* 5. sdcard size or path */
        String sdcard = null; 
        if(mSdcard != null) {
            sdcard = mSdcard.getLocationOrFormattedSize();
        }                 
        
        /* FINAL: create avd */        
        info("Creating AVD name=\"%s\", target=\"%s\", abi=%s, snapshot=%b, skin=%s, sdcard=%s", 
              getName(), getTarget(), abiType, mEnableSnapshot, skin, sdcard);
        
        AvdInfo info = avdManager.createAvd(avdPath, getName(), target, abiType, skin, sdcard,
                                            mHardwareConfig, mEnableSnapshot, mRemovePrevious, false,
                                            getTestManager().getSdkLogger());
        
        if(info == null) {
            error("Failed to create AVD.");
        } else {
            info("Created AVD \"%s\".", getName());
        }
    }
}
