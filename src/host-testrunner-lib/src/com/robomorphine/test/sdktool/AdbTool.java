package com.robomorphine.test.sdktool;

import java.io.File;

public class AdbTool extends SdkTool {
    
    private static final String RELATIVE_PATH = "platform-tools/adb";
    
    AdbTool(File sdkPath, ToolsManager manager) {
        super(new File(sdkPath, RELATIVE_PATH), manager);
    }
}
