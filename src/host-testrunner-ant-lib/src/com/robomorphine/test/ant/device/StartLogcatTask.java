package com.robomorphine.test.ant.device;

import com.android.ddmlib.IDevice;
import com.robomorphine.test.ant.BaseTask;
import com.robomorphine.test.emulator.RemoteLogcat;

import org.apache.tools.ant.BuildException;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class StartLogcatTask extends BaseTask {

    public enum LogLevel {
        Verbose("V"), 
        Debug("D"), 
        Info("I"), 
        Warn("W"), 
        Error("E"), 
        Fatal("F"), 
        Silent("S"); 
        
        private final String mSymbol;
        LogLevel(String symbol) {
            mSymbol = symbol;
        }
        
        public String getSymbol() {
            return mSymbol;
        }
    };
    
    public static class LogcatTag {
        private String mName;
        private LogLevel mLevel;
        
        public void setName(String name) {
            mName = name;
        }
        
        public String getName() {
            return mName;
        }
        
        public void setLevel(String levelName) {
            for(LogLevel level : LogLevel.values()) {
                if(level.name().equalsIgnoreCase(levelName)) {
                    mLevel = level;
                }
            }
            if(mLevel == null) {
                throw new IllegalArgumentException("Invalid level name: " + levelName);
            }
        }
        
        public LogLevel getLevel() {
            return mLevel;
        }
        
        public void verify(BaseTask task) {
            if(mName == null) {
                task.error("Tag name is not specified.");
            }
            if(mLevel == null) {
                task.error("Tag level is not specified.");
            }
        }
    }
    
    private static String [] sKnownBufferName = new String [] { 
        "main", "system", "radio", "events" 
    };
    
    private static String [] sKnownFormats = new String [] { 
        "brief", "process", "tag", "thread", "raw", "time", "threadtime", "long"
    };
    
    private String mReferenceName = null;
    private String mName;
    private File mFile = null;
    private String mBufferName = null;
    private String mFormat = null;
    private boolean mBinary = false;
    private boolean mSilent = false;
    private boolean mDump = false;
    private List<LogcatTag> mTags = new LinkedList<LogcatTag>();
    
    public void setId(String name) {
        mReferenceName = name;
    }
    
    public void setFile(File path) {
        mFile = path;
    }
    
    public void setBuffer(String name) {
        mBufferName = name;
    }
    
    public void setFormat(String format) {
        mFormat = format;
    }
    
    public void setBinary(boolean binary) {
        mBinary = binary;
    }
    
    public void setSilent(boolean silent) {
        mSilent = silent;
    }
    
    public void setDump(boolean dump) {
        mDump = dump;
    }
    
    public void addConfiguredTag(LogcatTag tag) {
        tag.verify(this);
        mTags.add(tag);
    }
    
     
    @Override
    public void execute() throws BuildException {
        
        if(mReferenceName != null) {
            mName = String.format("\"%s\"", mReferenceName);
        }
        
        if(mFile == null) {
            error("Logcat %s file is not specified. Use \"file\" attribute.", mName);
        }
        
        IDevice device = getDevice();
        RemoteLogcat logcat = new RemoteLogcat(getAdb(), device.getSerialNumber(), mFile, 
                                               getTestManager().getLogger());
        
        if(mSilent) {
            logcat.addArg("-s");
        }
        
        if(mFormat != null) {
            boolean hasFormat = false;
            
            StringBuilder knownFormats = new StringBuilder();
            for(String knownFormat : sKnownFormats) {
                if(knownFormats.length() > 0) {
                    knownFormats.append(", ");
                }
                knownFormats.append(knownFormat);                
                if(knownFormat.equals(mFormat)) {
                    hasFormat = true;
                    break;
                }
            }
            if(!hasFormat) {
                warn("Unknown logcat %s format: %s", mName, mFormat);                
                warn("Known formats are: %s", knownFormats.toString());
            }
            logcat.addArg("-v " + mFormat);
        }
        
        if(mDump) {
            logcat.addArg("-d");
        }
        
        if(mBufferName != null) {
            boolean hasName = false;
            StringBuilder knownNames = new StringBuilder();
            for(String knownName : sKnownBufferName) {
                if(knownNames.length() > 0) {
                    knownNames.append(", ");
                }
                knownNames.append(knownName);                
                if(knownName.equals(mBufferName)) {
                    hasName = true;
                    break;
                }
            }
            if(!hasName) {
                warn("Unknown logcat %s buffer name: %s", mName, mBufferName);                
                warn("Known buffer names are: %s", knownNames.toString());
            }
            logcat.addArg("-b " + mBufferName);
        }
        
        if(mBinary) {
            logcat.addArg("-B");
        }
        
        for(LogcatTag tag : mTags) {
            logcat.addArg(tag.getName() + ":" + tag.getLevel().getSymbol());
        }
        
        if(mReferenceName != null) {
            getProject().addReference(mReferenceName, logcat);
        }
        
        try {
            info("Starting logcat %s", mName);
            logcat.start(!mDump);
        } catch(Exception ex) {
            error(ex, "Failed to start logcat: %s", ex.getMessage());
        }
    }
}
