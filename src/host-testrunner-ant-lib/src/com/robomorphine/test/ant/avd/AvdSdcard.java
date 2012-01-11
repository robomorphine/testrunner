package com.robomorphine.test.ant.avd;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to specify sdcard size or existing sdcard image path. 
 * 
 * If both path and sdcard size are specified, then sdcard size is ignored.
 * 
 * Size: 
 * The size should be specified in format [value][modifier]. Where [value]
 * is any number, and modifier can be one of [K, KB, M, MB, G, GB]. If modifier
 * is not specified, "M" is assumed. 
 * 
 * Examples: 9999K, 10M, 13G, 10, 115mb, 23KB
 * 
 * Path:
 * A valid path to sdcard image that should be used when creating avd. 
 */
public class AvdSdcard {
    
    private final static String [] KB_MODIFIERS = { "k", "kb" };
    private final static String [] MB_MODIFIERS = { "m", "mb" };
    private final static String [] GB_MODIFIERS = { "g", "gb" };
    private final static long KB_SCALE = 1 << 10;
    private final static long MB_SCALE = 1 << 20;
    private final static long GB_SCALE = 1 << 30;
    private final static Pattern SDCARD_SIZE_PATTERN = Pattern.compile("(\\d+)(.*)"); 
    
    private final AvdConfigTask mAvdTask;    
    public AvdSdcard(AvdConfigTask task) {
        mAvdTask = task;
    }
    
    private static boolean isModifierFrom(String [] modifiers, String modifier) {
        for(String curModifier : modifiers) {
            if(curModifier.equalsIgnoreCase(modifier)) {
                return true;
            }
        }
        return false;
    }
    
    private long getScale(String modifier) {
        if(modifier == null || modifier.trim().isEmpty()) {
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
        mAvdTask.error("Invalid sdcard size modifier: \"%s\".", modifier);
        return KB_SCALE;
    }
    
    private long mSize = -1;  
    private File mPath = null;
   
    public void setSize(String size) {
        size = size.trim().toUpperCase();
        
        Matcher m = SDCARD_SIZE_PATTERN.matcher(size);
        if(!m.matches()) {
            mAvdTask.error("Value \"%s\" doesn't look like sdcard size.", size);
        }
        
        String value = m.group(1);
        String modifier = m.group(2);
        
        try {
            long scale = getScale(modifier);
            long parsedValue = Integer.parseInt(value);
            mSize = scale * parsedValue;
        } catch(NumberFormatException ex) {
            mAvdTask.error("Failed to parse sdcard size: %s", size);
        }
    }
    
    public long getSize() {
        return mSize;
    }
    
    /**
     * Location of existing sdcard image. If this is set, sdcard size will be ignored.
     */
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
