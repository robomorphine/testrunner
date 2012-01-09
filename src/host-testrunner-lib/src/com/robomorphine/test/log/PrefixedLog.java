package com.robomorphine.test.log;

public class PrefixedLog implements ILog {
    
    private final ILog mLog;
    private final String mTag;
    
    public PrefixedLog(String tag, ILog logger) {
        mTag = tag;
        mLog = logger;
    }
    
    @Override
    public void v(String format, Object... args) {
        mLog.v(mTag + ": " + format, args);
    }
    
    @Override
    public void i(String format, Object... args) {
        mLog.i(mTag + ": " + format, args);
    }
    
    @Override
    public void w(String format, Object... args) {
        mLog.w(mTag + ": " + format, args);
    }
    
    @Override
    public void e(Throwable ex, String format, Object... args) {
        mLog.e(ex, mTag + ": " + format, args);
    }
}
