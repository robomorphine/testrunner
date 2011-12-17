package com.robomorphine.test.ant;

import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.internal.avd.AvdManager;

import org.apache.tools.ant.BuildException;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAvdTask extends BaseTask {
    
    public class Sdcard {
        private final String [] KB_MODIFIERS = { "k", "kb" };        
        private final String [] MB_MODIFIERS = { "m", "mb" };
        private final String [] GB_MODIFIERS = { "g", "gb" };
        private final long KB_SCALE = 1 << 10;
        private final long MB_SCALE = 1 << 20;
        private final long GB_SCALE = 1 << 30;
        private final Pattern SDCARD_SIZE_PATTERN = Pattern.compile("(\\d+)(.*)"); 
        
        private boolean isModifierFrom(String [] modifiers, String modifier) {
            for(String curModifier : modifiers) {
                if(curModifier.equalsIgnoreCase(modifier)) {
                    return true;
                }
            }
            return false;
        }
        
        private long getScale(String modifier) {
            if(modifier == null || modifier.trim().length() == 0) {
                return MB_SCALE;
            }
            if(isModifierFrom(KB_MODIFIERS, modifier)) {
                return KB_SCALE; 
            }
            if(isModifierFrom(MB_MODIFIERS, modifier)) {
                return MB_SCALE; 
            }
            if(isModifierFrom(GB_MODIFIERS, modifier)) {
                return GB_SCALE; 
            }
            error("Invalid sdcard size modifier: \"%s\".", modifier);
            return KB_SCALE;
        }
        
        private long mSize = -1;  
        private File mPath = null;
        
        public void setSize(String size) {
            size = size.trim().toUpperCase();
            
            Matcher m = SDCARD_SIZE_PATTERN.matcher(size);
            if(!m.matches()) {
                error("Value \"%s\" doesn't look like sdcard size.", size);
            }
            
            String value = m.group(1);
            String modifier = m.group(2);
            
            try {
                long scale = getScale(modifier);
                long parsedValue = Integer.parseInt(value);
                mSize = scale * parsedValue;
            } catch(NumberFormatException ex) {
                error("Failed to parse sdcard size: %s", size);
            }
        }
        
        public long getSize() {
            return mSize;
        }
        
        public void setLocation(File path) {
            mPath = path;
        }
        
        public File getLocation() {
            return mPath;
        }
        
        public String getLocationOrFormattedSize() {
            if(mPath != null) {
                return mPath.getAbsolutePath();
            }
            if(mSize > 0) {
                return String.format("%dK", mSize/KB_SCALE);
            } else {
                return null;
            }
        }
    }    
    
    public class Display {
     
        String mSkin;
        
        public void setSkin(String skin) {
            IAndroidTarget target = getResolvedTarget();
            for(String curSkin : target.getSkins()) {
                if(curSkin.equals(skin)) {
                    mSkin = skin;
                    return;
                }
            }
            error("Skin \"%s\" is not valid for target \"%s\".", skin, target.getName());
        }
    }
    
    private String mName;
    private String mTarget;
    private IAndroidTarget mResolvedTarget;
    
    private HashMap<String, String> mHardwareConfig = new HashMap<String, String>();
    private boolean mEnableSnapshot = false;
    private boolean mRemovePrevious = true;    
         
    private Sdcard mSdcard = new Sdcard();
    private Display mDisplay = new Display();
    
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
    
    public Sdcard createSdcard() {    
        return mSdcard;
    }
    
    public Display createDisplay() {
        return mDisplay;
    }
    
    @Override
    public void execute() throws BuildException {
        AvdManager avdManager = getTestManager().getAvdManager();
        IAndroidTarget target = getResolvedTarget();
        
        File avdPath = null;
        try {
            avdPath = AvdInfo.getDefaultAvdFolder(avdManager, getName());
        } catch(AndroidLocationException ex) {
            error(ex, "Failed to determine path for new avd.");
        }
        
        String abiType = null;
        ISystemImage [] images = target.getSystemImages();
        if(images == null || images.length < 1) {
            error("No system images are available for target: %s", getTarget());
        }
        abiType = images[0].getAbiType();
                 
        info("Creating AVD name=\"%s\", target=\"%s\", sdcard=%s", 
              getName(), getTarget(), mSdcard.getLocationOrFormattedSize());
        
        AvdInfo info = avdManager.createAvd(avdPath, getName(), target, abiType, "WVGA800", 
                                            mSdcard.getLocationOrFormattedSize(),
                                            mHardwareConfig, mEnableSnapshot, mRemovePrevious, false,
                                            getTestManager().getSdkLogger());
        
        if(info == null) {
            error("Failed to create AVD.");
        } else {
            info("Created AVD \"%s\".", getName());
        }
    }
}
