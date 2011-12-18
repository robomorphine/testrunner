package com.robomorphine.test.ant.avd;

import com.android.sdklib.IAndroidTarget;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configures avd screen. It has two parameters: resolution and density.
 * 
 * ------------------------------------
 * Resolution may have three different types of input:
 * 1. Resolution specification: 100x100, 200dp*200px
 * 2. Resolution alias: small, normal, large, xlarge
 * 3. Skin
 * 
 * 
 * 1. Resolution specification has format: <size><divider><size>.
 *   - Where <divider> can be on of: "x", "/", "\", "*"
 *   - Where first <size> represents width, second represents height.  
 *   - Where <size> consists of two parts <number>[<modififer>]
 *      # <number> is any number greater than 100      
 *      # <modifier> is one of: "dp" or "px"
 *      # <modifier> is optional, "px" is used when <modifier> is not specified.
 *      # if <modifier> is "dp", density must be specified
 *      
 * Examples: 100x100, 100dp*100px, 100/200px, 300DP*200
 * 
 * 2. Resolution alias are a few aliases that represent different screen sizes. 
 * Currently these aliases are supported:
 *    xlarge  - 960dp x 720dp
 *    large   - 640dp x 480dp
 *    normal  - 470dp x 320dp
 *    small   - 426dp x 320dp
 *    
 * And because resolution alias uses "dp", density must be specified when using resoltion alias.
 * 
 * 3. Skin name is one of supported skin by current target. This is taken from sdk.
 * 
 * ------------------------------------
 * Density may be any number or one of next supported density aliases:
 *  ldpi - 120
 *  mdpi - 160
 *  hdpi - 240
 *  xhdpi - 320
 *  
 */
public class AvdScreen {
    
    private static final Pattern DENSITY_NUMERIC_VALUE = Pattern.compile("\\d+");
    private static final int NORMAL_DENSITY = 160;
    private static final Map<String, Integer> DENISTY_ALIASES;
    static {
        HashMap<String, Integer> aliases = new HashMap<String, Integer>();
        aliases.put("ldpi".toLowerCase(), 120);
        aliases.put("mdpi".toLowerCase(), 160);        
        aliases.put("hdpi".toLowerCase(), 240);
        aliases.put("xhdpi".toLowerCase(), 320);
        DENISTY_ALIASES = Collections.unmodifiableMap(aliases);
    }
    
    /**
     * Examples: 100x100, 100/100, 100\100, 100*100, 100px*200dp, 100*200PX, etc
     */
    private static final Pattern RESOLUTION_NUMERIC_VALUE = 
            Pattern.compile("(?i)(\\d\\d+)(px|dp|)[x|\\\\|/|\\*](\\d\\d+)(px|dp|)");    
    private static final String DP_MODIFIER = "dp";
    
    private static final Map<String, String> RESOLUTION_ALIASES;
    static {
        HashMap<String, String> aliases = new HashMap<String, String>();
        aliases.put("small".toLowerCase(), "426dp*320dp");
        aliases.put("normal".toLowerCase(), "470dp*320dp");
        aliases.put("large".toLowerCase(), "640dp*480dp");
        aliases.put("xlarge".toLowerCase(), "960dp*720dp");
        RESOLUTION_ALIASES = Collections.unmodifiableMap(aliases);
    }
    
    private final AvdConfigTask mAvdTask;    
    public AvdScreen(AvdConfigTask task) {
        mAvdTask = task;
    }
    
    String mResolution = null;
    String mResolvedResolution = null;
    int mDensity = NORMAL_DENSITY;
    
    /**
     * Density. Can be specified using a number or one of the aliases.
     * Examples: 120, 160, 240, 320, mdpi, xhdpi, medium, extrahigh
     * (for list of aliases see mDensityAliases).
     */
    public void setDensity(String density) {
        Matcher m = DENSITY_NUMERIC_VALUE.matcher(density);
        if(m.matches()) {
            try {
                mDensity = Integer.parseInt(density);
            } catch(NumberFormatException ex){
                mAvdTask.error("Failed to parse %s as density.", density);
            }
        } else {
            Integer value = DENISTY_ALIASES.get(density.toLowerCase());
            if(value == null) {
                mAvdTask.error("Density alias is not recognized: %s", density);
            }
            mDensity = value;
        }
    }
    
    public boolean hasDensity() {
        return mDensity > 0;
    }
    
    public int getDensity() {
        return mDensity;
    }
    
    public int convertToPx(int dp) {
        if(!hasDensity()) {
            mAvdTask.error("Screen density is not specified. " + 
                                  "If you've used \"dp\" in resolution you have to specify density.");
        }
        return (int)(1.0 * getDensity() / NORMAL_DENSITY * dp);
    }
    
    public int convertToPx(String value, String modifier) {
        try {
            int size = Integer.parseInt(value);
            if(DP_MODIFIER.equalsIgnoreCase(modifier.trim())) {
                size = convertToPx(size);
            }
            return size;
        } catch(NumberFormatException ex) {
            mAvdTask.error(ex, "Failed to parse resolution's size: %s.", value);            
        }
        return 0;
    }
    
    public void setResolution(String resoltuion) {
        String aliased = RESOLUTION_ALIASES.get(resoltuion.toLowerCase());
        if(aliased != null) {
            mResolution = aliased;
        } else {
            mResolution = resoltuion;
        }
    }
    
    public String getResolution() {
        if(mResolution == null) {
            return null;
        }
        
        if(mResolvedResolution != null) {
            return mResolvedResolution;
        }
        
        Matcher matcher = RESOLUTION_NUMERIC_VALUE.matcher(mResolution);
        if(matcher.matches()) {
            String widthValue = matcher.group(1);
            String widthModifier = matcher.group(2);
            String heightValue = matcher.group(3);
            String heightModifier = matcher.group(4);
            
            int widthPx = convertToPx(widthValue, widthModifier);
            int heightPx = convertToPx(heightValue, heightModifier);
            mResolvedResolution = String.format("%dx%d", widthPx, heightPx);
           
        } else {
            boolean verified = false;
            
            IAndroidTarget target = mAvdTask.getResolvedTarget();
            while(target != null && !verified) {
                for(String skin : target.getSkins()) {
                    if(skin.equals(mResolution)) {
                        verified = true;
                        break;
                    }
                }
                target = target.getParent();
            }
            
            if(!verified) {
                invalidResolution(mResolution); 
            }
            mResolvedResolution = mResolution;
        }
        
        return mResolvedResolution;
    }
    
    private void invalidResolution(String resolution) {
        StringBuilder resolutionAliases = new StringBuilder();
        for(String alias : RESOLUTION_ALIASES.keySet()) {
            if(resolutionAliases.length() > 0) {
                resolutionAliases.append(", ");
            }
            resolutionAliases.append(alias);
        }
        
        
        IAndroidTarget target = mAvdTask.getResolvedTarget();
        LinkedHashSet<String> skinSet = new LinkedHashSet<String>(); 
        while(target != null) {
            for(String skin : target.getSkins()) {
                skinSet.add(skin);
            }
            target = target.getParent();
        }
        
        StringBuilder skins = new StringBuilder();
        for(String skin : skinSet) {
            if(skins.length() > 0) {
                skins.append(", ");                
            }
            skins.append(skin);
        }
        
        mAvdTask.error("Specified resolution \"%s\" is not a valid. \n"+
                "Make sure it's a valid resolution alias (one of: %s); \n" + 
                "Or a valid skin for current target %s (one of: %s); \n"+
                "Or a valid resolution specifier (like: 100x200 or 120DP*300px).",
                resolution, resolutionAliases, mAvdTask.getTarget(), skins);    
     }
}
