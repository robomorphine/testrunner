package com.robomorphine.test.sdktool;

import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.StdLog;
import com.robomorphine.test.sdktool.SdkTool.Result;
import com.robomorphine.test.sdktool.SdkTool.ToolListener;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class SdkToolTest extends TestCase {
    
    private static class TestToolListener implements ToolListener {
        @Override
        public void onStarted() {
            System.out.println("Tool started...");            
        }
        
        @Override
        public void onCompleted(int exitCode) {
            System.out.println("Tool completed: " + exitCode);
        }
        
        @Override
        public void onStdOutput(String outLine) {
            System.out.println("Tool out: " + outLine);
        }
        
        @Override
        public void onStdError(String errLine) {
            System.out.println("Tool err: " + errLine);
        }
    }
    
    public void testSdkTool_async() throws IOException, InterruptedException {
        File sdkPath = new File("r:\\repository\\dev\\bin\\android-sdk");
        File exePath = new File("r:\\repository\\dev\\bin\\android-sdk\\tools\\emulator-x86");
        ILog logger = new StdLog();
        
        final ToolsManager manager = new ToolsManager(sdkPath, logger);
        SdkTool tool = manager.createTool(exePath);
        
        tool.addArgument("@Android-2.2", "-verbose");
        tool.setStoreOutput(false);
        tool.setListner(new TestToolListener());
        tool.startExecution();
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException ex) {
                    
                }
                manager.terminate();                
            }
        });
        thread.start();
        
        tool.waitForCompletion();
        
        Result result = tool.getResult();
        System.out.println("Result: " + result.getExitCode());
        System.out.println("Out: " + result.getStdOut());
        System.out.println("Err: " + result.getStdErr());
    }
    
    public void testSdkTool_sync() throws IOException, InterruptedException {
        File path = new File("r:\\repository\\dev\\bin\\android-sdk\\tools\\emulator");
        SdkTool tool = new SdkTool(path, null);
        tool.setListner(new TestToolListener());
        Result result = tool.execute();
        System.out.println("Result: " + result.getExitCode());
        System.out.println("Out: " + result.getStdOut());
        System.out.println("Err: " + result.getStdErr());
    }
        
    public void testAapt_sync() throws IOException, InterruptedException {
        File path = new File("r:\\repository\\dev\\bin\\android-sdk\\platform-tools\\aapt");
        SdkTool tool = new SdkTool(path, null);
        tool.setListner(new TestToolListener());
        Result result = tool.execute();
        System.out.println("Result: " + result.getExitCode());
        System.out.println("Out: " + result.getStdOut());
        System.out.println("Err: " + result.getStdErr());
    }
    
    public void testAapt_packageName() throws IOException {
        File path = new File("r:\\repository\\dev\\bin\\android-sdk\\");
        ILog logger = new StdLog();
        ToolsManager manager = new ToolsManager(path, logger);
        
        File apkFile = new File("r:\\repository\\dev\\src\\projects\\github\\robomorphine-testrunner\\bin\\debug\\tester.app.apk");
        String pkg = manager.createAaptTool().getPacakgeName(apkFile);
        assertEquals("com.robomorphine.tester.app", pkg);
    }
}
