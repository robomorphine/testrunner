package com.robomorphine.test.emulator;

import com.robomorphine.test.TestManager;
import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.PrefixedLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmulatorConsole {
    
    private final static int DEFAULT_CONNECT_TIMEOUT = 5000;
    private final static int DEFAULT_SO_TIMEOUT = 10000;
    
    private final static String EMU_OK_RESPONSE = "OK";
    private final static String EMU_OK_RESPONSE_PREFIX = "OK:";
    private final static String EMU_KO_RESPONSE_PREFIX = "KO:";
  
    
    private final ILog mLog;
            
    public EmulatorConsole(TestManager testManager) {
        mLog = new PrefixedLog(EmulatorConsole.class.getSimpleName(), testManager.getLogger());
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
            InetSocketAddress addr = new InetSocketAddress("127.0.0.1", port);
            
            Socket socket = new Socket();
            socket.setSoTimeout(DEFAULT_SO_TIMEOUT);
            socket.connect(addr, DEFAULT_CONNECT_TIMEOUT);
            
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
    /**         Built-in commands             **/      
    /*******************************************/
    
    
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
}
