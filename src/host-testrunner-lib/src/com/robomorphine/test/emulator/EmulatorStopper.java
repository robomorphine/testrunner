package com.robomorphine.test.emulator;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.sdktool.AdbTool;
import com.robomorphine.test.sdktool.SdkTool.Result;
import com.robomorphine.test.sdktool.ToolsManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmulatorStopper {
    
    private final static int ADB_KILL_TRIES = 3;
    private final static int EMU_CONSOLE_KILL_TRIES = 5;
    
    private final ILog mLog;
    private final String mSerialNo;
    private final TestManager mTestManager;
            
    public EmulatorStopper(String serialNo, TestManager testManager) {
        mLog = testManager.getLogger();
        mSerialNo = serialNo;
        mTestManager = testManager;
    }    
        
    public boolean stop() {
        if(!mTestManager.isAdbConnected()) {
            throw new IllegalStateException("ADB is not connected");
        }
        
        AndroidDebugBridge adb = mTestManager.getAndroidDebugBridge();
        IDevice device = null;
        for(IDevice curDevice : adb.getDevices()) {
            if(curDevice.getSerialNumber().endsWith(mSerialNo)) {
                if(!curDevice.isEmulator()) {
                    throw new IllegalArgumentException("Not an emulator: " + mSerialNo);
                }
                device = curDevice;
                break;
            }
        }
        if(device == null) {
            mLog.warning("Emulators with serial %s was not found. " +
                         "Assuming it has already been stopped.",
                         mSerialNo);
            return true;
        }
        
        /* Let's ask adb to kill the emulator*/        
        for(int i = 0; i < ADB_KILL_TRIES; i++) {
            mLog.info("Killing emulator \"%s\" using adb.", mSerialNo); 
            if(!stopUsingAdb()) {
                mLog.info("Failed to stop emulator \"%s\" using avd.", mSerialNo);
            }
                
            if(!isEmulatorRunning()) {
                 mLog.info("Emulator \"%s\" is no longer running.", mSerialNo);
                 return true;
            }
            
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                //ignore
            }
        }
        
        /* Let's kill emulator using emulator console */        
        for(int i = 0; i < EMU_CONSOLE_KILL_TRIES; i++) {
            mLog.info("Killing emulator \"%s\" using emulator console.", mSerialNo); 
            if(!stopUsingEmulatorConsole()) {
                mLog.info("Failed to stop emulator \"%s\" using emulator console.", mSerialNo);
            }
                
            if(!isEmulatorRunning()) {
                 mLog.info("Emulator \"%s\" is no longer running.", mSerialNo);
                 return true;
            }
            
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                //ignore
            }
        }
        
        return false;
    }
    
    private boolean isEmulatorRunning() {
        for(IDevice device : mTestManager.getAndroidDebugBridge().getDevices()) {
            if(device.getSerialNumber().endsWith(mSerialNo)) {
                return true;
            }
        }
        return false;
    }
    
    boolean stopUsingAdb() {
       ToolsManager toolsManager = mTestManager.getToolsManager();
       AdbTool adbTool = toolsManager.createAdbTool();
       
       adbTool.addArgument("-s", mSerialNo, "emu", "kill");
       try {
           Result result = adbTool.execute();
           return result.getExitCode() == 0;
       } catch(Exception ex) {
           mLog.error(ex, "Failed to stop emulator \"%s\" using adb.", mSerialNo);
           return false;
       }
    }
    
    boolean stopUsingEmulatorConsole() {
        String serial = mSerialNo.toLowerCase();
        Pattern pattern = Pattern.compile("emulator-(\\d+)");
        Matcher matcher = pattern.matcher(serial);
        if(!matcher.matches()) {
            String msg = "Serial number \"%s\" in invalid. Failed to extract port number.";
            mLog.error(new IllegalArgumentException(), msg, mSerialNo);
            return false;
        }
        String strPort = matcher.group(1);
        int port = 0;
        try {
            port = Integer.parseInt(strPort);
        } catch(NumberFormatException ex) {
            mLog.error(ex, "Failed to convert \"%s\" to port number.", strPort);
            return false;
        }
        
        try {
            Socket socket = new Socket("127.0.0.1", port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            /* Currently emulator console on Windows 7 does not accept first command.
             * We sending new line to make this first ignored command a noop. 
             * And then send a need "kill" command that will take effect. */
            out.println();
            mLog.info("Emulator console: >");
            out.println("kill");
            mLog.info("Emulator console: > kill");
            
            /* Must read responses from emulator or otherwise pending 
             * commands will not get executed (including "kill"). */
            String line;
            while((line = in.readLine()) != null) {
                mLog.info("Emulator console: < %s", line);
            }
            
            socket.close();            
        } catch (Exception ex) {
            if(isEmulatorRunning()) {
                mLog.error(ex, "Failed to communicate with emulator \"%s\" console.", mSerialNo);
                return false;
            }
        }
        return true;
    }
}
