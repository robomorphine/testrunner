package com.robomorphine.test.sdktool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AaptTool extends SdkTool {
    
    private static final String RELATIVE_PATH = "platform-tools/aapt";
    
    AaptTool(File sdkPath, ToolsManager manager) {
        super(new File(sdkPath, RELATIVE_PATH), manager);
    }
    
    static String extractPackageName(Result result) {
        String out = result.getStdOut();
        if(out == null) {
            return out;
        }
        
        Pattern pattern = Pattern.compile(".*name=\\'(.*?)\\'.*");
        for(String line : out.split("\n")){
            if(line.startsWith("package:")) {
                Matcher matcher = pattern.matcher(line);
                if(matcher.matches()) {
                    return matcher.group(1);
                } else {
                    return null;
                }
            }
        }   
        return null;
    }
    
    public String getPacakgeName(File apkFile) throws IOException {
        if(!apkFile.exists()) {
            throw new FileNotFoundException(apkFile.getAbsolutePath());
        }
        addArgument("d", "badging", apkFile.getAbsolutePath());
        try {
            Result result = execute();
            return extractPackageName(result);   
        } catch(InterruptedException ex) {
            throw new IOException("Failed to wait for execution completion.");
        }        
    }
}
