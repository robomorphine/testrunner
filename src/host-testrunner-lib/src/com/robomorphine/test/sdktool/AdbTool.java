package com.robomorphine.test.sdktool;

import java.io.File;

public class AdbTool extends SdkTool {  // NOPMD 
    
    private static final String RELATIVE_PATH = "platform-tools/adb";
    
    public static final File getAdbPath(File sdkPath) {
        return new File(sdkPath, RELATIVE_PATH);
    }
    
    protected AdbTool(File sdkPath, ToolsManager manager) {
        super(getAdbPath(sdkPath), manager);
    }
}
