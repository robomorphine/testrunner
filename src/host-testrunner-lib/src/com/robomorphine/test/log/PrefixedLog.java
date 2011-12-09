package com.robomorphine.test.log;

public class PrefixedLog implements ILog {
    
    private final ILog mLog;
    private final String mTag;
    
    public PrefixedLog(String tag, ILog logger) {
        mTag = tag;
        mLog = logger;
    }
    
    @Override
    public void info(String format, Object... args) {
        mLog.info(mTag + ": " + format, args);
    }
    
    @Override
    public void warning(String format, Object... args) {
        mLog.warning(mTag + ": " + format, args);
    }
    
    @Override
    public void error(Throwable ex, String format, Object... args) {
        mLog.error(ex, mTag + ": " + format, args);
    }
}
