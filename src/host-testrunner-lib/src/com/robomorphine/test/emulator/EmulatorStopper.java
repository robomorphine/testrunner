package com.robomorphine.test.emulator;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.robomorphine.test.TestManager;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.PrefixedLog;
import com.robomorphine.test.sdktool.AdbTool;
import com.robomorphine.test.sdktool.SdkTool.Result;
import com.robomorphine.test.sdktool.ToolsManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmulatorStopper {
    
    private final static long DEFAULT_TIMEOUT = 3000;
    
    private final static String EMU_OK_RESPONSE = "OK";
    private final static String EMU_OK_RESPONSE_PREFIX = "OK:";
    private final static String EMU_KO_RESPONSE_PREFIX = "KO:";
    
    private final ILog mLog;
    private final TestManager mTestManager;
            
    public EmulatorStopper(TestManager testManager) {
        mLog = new PrefixedLog(EmulatorStopper.class.getSimpleName(), testManager.getLogger());
        mTestManager = testManager;
    }    
    
    /**
     * Extracts port number from serial number of emulator.
     * @return port number or -1 if extraction failed
     */
    public int consoleExtractPort(String serialNo) {
        String serial = serialNo.toLowerCase();
        Pattern pattern = Pattern.compile("emulator-(\\d+)");
        Matcher matcher = pattern.matcher(serial);
        if(!matcher.matches()) {
            String msg = "Serial number \"%s\" in invalid. Failed to extract port number.";
            mLog.error(new IllegalArgumentException(), msg, serialNo);
            return -1;
        }
        String strPort = matcher.group(1);
        int port = 0;
        try {
            port = Integer.parseInt(strPort);
        } catch(NumberFormatException ex) {
            mLog.error(ex, "Failed to convert \"%s\" to port number.", strPort);
            return -1;
        }
        return port;
    }
    
    /*******************************************/
    /**         Console Communication         **/      
    /*******************************************/
    
    private String consoleReadMessage(InputStream in) throws IOException {
        int read = 0;
        byte [] buffer = new byte[1024];
        
        StringBuilder response = new StringBuilder();
        while((read = in.read(buffer)) > 0) {
            String line = new String(buffer, 0, read);
            response.append(line);
            
            String lines [] = response.toString().split("\r?\n|\r");
            if(lines.length > 0) {
                String lastLine = lines[lines.length - 1];                
                if(lastLine.equals(EMU_OK_RESPONSE) || 
                   lastLine.startsWith(EMU_OK_RESPONSE_PREFIX)) {
                    break;
                }
                
                if(lastLine.startsWith(EMU_KO_RESPONSE_PREFIX)) {
                    break;
                }
            }
        }
        return response.toString();
    }
    
    private void consoleWriteMessage(OutputStream out, String msg) throws IOException {
        msg = msg.trim() + "\n";
        out.write(msg.getBytes());
    }
    
    private boolean consoleExtractStatus(String response) {
        String [] lines = response.split("\n");
        if(lines.length == 0) {
            return false;
        }
        String lastLine = lines[lines.length - 1].trim();
        return lastLine.equals(EMU_OK_RESPONSE) ||
               lastLine.startsWith(EMU_OK_RESPONSE_PREFIX);               
    }
    
    private String consoleExtractError(String response) {
        String [] lines = response.split("\n");
        if(lines.length == 0) {
            return "";
        }
        String lastLine = lines[lines.length - 1];
        if(lastLine.trim().startsWith(EMU_KO_RESPONSE_PREFIX)) {
            return lastLine;
        } else {
            return "Everything seems OK.";
        }
    }
    
    public boolean consoleRunCommand(int port, String cmd) {
        try {
            Socket socket = new Socket("127.0.0.1", port);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            
            String msg = consoleReadMessage(in);
            if(!consoleExtractStatus(msg)) {
                String error = consoleExtractError(msg);
                mLog.error(null, "Greeting failure. Response: %s", error);
                return false;
            }
                        
            /* workaround: first command always fails on windows */
            consoleWriteMessage(out, "help");
            msg = consoleReadMessage(in);
            
            mLog.info("Sending \"%s\" to emulator console...", cmd);
            consoleWriteMessage(out, cmd);
            msg = consoleReadMessage(in);
                        
            if(!consoleExtractStatus(msg)) {
                String error = consoleExtractError(msg);
                mLog.error(null, "Command failure. Response: %s", error);
                return false;
            }
            mLog.info("Emulator console response:\n%s", msg);
            return true;
        } catch(IOException ex) {
            mLog.error(ex, "Failed to run \"%s\" on emulator console.", cmd);
            return false;
        }       
    }
    
    /*******************************************/
    /**         Running Detection             **/      
    /*******************************************/
    
    /**
     * Detects if emulator is running using adb.
     * @param serialNo
     * @return true if running, false otherwise.
     */
    public boolean adbIsRunning(String serialNo) {
        AndroidDebugBridge adb = mTestManager.getAndroidDebugBridge();
        for(IDevice device : adb.getDevices()) {
            if(device.getSerialNumber().endsWith(serialNo)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Detects if emulator is running by connecting to its console.
     */
    public boolean consoleIsRunning(String serialNo) {
        int port = consoleExtractPort(serialNo);
        if(port < 0) {
            return false;
        }
        return consoleIsRunning(port);
    }
    
    public boolean consoleIsRunning(int port) {
        mLog.info("Sending command to console on %d port to detect if its alive...", port);        
        boolean res = consoleRunCommand(port, "power display");
        mLog.info("Conclusiong: emulator on port %d is %s.", port, res ? "alive" : "dead");
        return res;
    }
    
    public boolean isRunning(String serialNo) {
        return adbIsRunning(serialNo) || consoleIsRunning(serialNo);
    }
    
    /*******************************************/
    /**                Stop                   **/      
    /*******************************************/
    
    public boolean abdStop(String serialNo) {
       ToolsManager toolsManager = mTestManager.getToolsManager();
       AdbTool adbTool = toolsManager.createAdbTool();
       
       adbTool.addArgument("-s", serialNo, "emu", "kill");
       try {
           Result result = adbTool.execute();
           return result.getExitCode() == 0;
       } catch(Exception ex) {
           mLog.error(ex, "Failed to stop emulator \"%s\" using adb.", serialNo);
           return false;
       }
    }
    
    public boolean consoleStop(String serialNo) {
        int port = consoleExtractPort(serialNo);
        if(port < 0) {
            return false;
        }
        return consoleStop(port);
    }
    
    public boolean consoleStop(int port) {
        return consoleRunCommand(port, "kill");
    }
    
    public boolean waitForEmualtorStopped(String serialNo, long ms) {
        mLog.info("Waiting for emulator \"%s\" to stop.", serialNo);
        long end = System.currentTimeMillis() + ms;
        long sleepStep = 1000;
        while(end > System.currentTimeMillis()) {
            if(!isRunning(serialNo)) {
                return true;
            }
            mLog.info("Emulator \"%s\" is still alive...", serialNo);
            
            long delta = end - System.currentTimeMillis();
            if(delta <= 0) {
                return false;
            }
            
            long sleep = sleepStep;
            if(sleep > delta) {
                sleep = delta;
            }            
            
            try {
                Thread.sleep(sleep);
            } catch(InterruptedException ex) {
                return false;
            }
        }
        return false;
    }    
    
    public boolean stop(String serialNo) {
        /* check adb connection */
        if(!mTestManager.isAdbConnected()) {
            throw new IllegalStateException("ADB is not connected");
        }
        
        if(!isRunning(serialNo)) {
            mLog.info("Emualtor \"%s\" is not running. Nothing to stop.", serialNo);
            return true;
        }
        
        /* verify serial represents emulator, and not a real device */
        AndroidDebugBridge adb = mTestManager.getAndroidDebugBridge();
        boolean skipAdbStop = true;
        for(IDevice curDevice : adb.getDevices()) {
            if(curDevice.getSerialNumber().endsWith(serialNo)) {
                if(!curDevice.isEmulator()) {
                    throw new IllegalArgumentException("Not an emulator: " + serialNo);
                }
                skipAdbStop = false;                
                break;
            }
        }
        
        if(!skipAdbStop) {
            /* Killing emulator using adb's embedded control over emulator console */        
            mLog.info("Killing emulator \"%s\" using adb.", serialNo); 
            if(abdStop(serialNo)) {
                if(waitForEmualtorStopped(serialNo, DEFAULT_TIMEOUT)) {
                    mLog.info("Emulator \"%s\" is no longer running.", serialNo);
                    return true;
                }        
            } else {
                mLog.info("Failed to stop emulator \"%s\" using avd.", serialNo);                  
            }        
        } else {
            mLog.warning("Emulator \"%s\" is not visible from adb.", serialNo);
        }
        
        /* Let's kill emulator using emulator console */        
        mLog.info("Killing emulator \"%s\" using emulator console.", serialNo); 
        if(consoleStop(serialNo)) {
            if(waitForEmualtorStopped(serialNo, DEFAULT_TIMEOUT)) {
                mLog.info("Emulator \"%s\" is no longer running.", serialNo);
                return true;
            }
        } else {
            mLog.info("Failed to stop emulator \"%s\" using emulator console.", serialNo);
        }
        
        mLog.error(null, "I really tried to kill \"%s\" emulator. But failed.", serialNo);        
        return false;
    }
    
    public boolean stopMultiple(int firstPort, int lastPort) {        
        if(firstPort % 2 != 0) {
            firstPort++;
        }
        
        mLog.info("Starting to kill emulators on ports %d - %d.", firstPort, lastPort);
        
        boolean ok = true;
        for(int i = firstPort; i <= lastPort; i+=2) {
            String serialNo = String.format("emulator-%d", i); 
            if(isRunning(serialNo)) {
                if(!stop(serialNo)) {
                    ok = false;
                }
            }
        }
        
        return ok;
    }
    
    public boolean stopAll() {
        mLog.info("Killing all running emulators.");
        return stopMultiple(5554, 5584);
    }
}
