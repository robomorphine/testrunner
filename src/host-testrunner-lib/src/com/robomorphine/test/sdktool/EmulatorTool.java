package com.robomorphine.test.sdktool;

import java.io.File;

public class EmulatorTool extends SdkTool {
    
    private static final String RELATIVE_PATH = "tools/emulator";
    
    EmulatorTool(File sdkPath, ToolsManager manager) {
        super(new File(sdkPath, RELATIVE_PATH), manager);
        setStoreOutput(false);
    }
    
    
}
