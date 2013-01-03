package com.robomorphine.test.log;

import com.android.utils.StdLogger;

public class StdSdkLogger implements ILog  {
    
    private final StdLogger mStdLogger = new StdLogger(com.android.utils.StdLogger.Level.VERBOSE);
    private LogLevel mLogLevel = LogLevel.Verbose;
    
    public void setLevel(LogLevel level) {
        mLogLevel = level;
    }
    
    public LogLevel getLevel() {
        return mLogLevel;
    }
    
    private boolean shouldPrint(LogLevel level) {
        return level.getLevel() >= mLogLevel.getLevel();
    }
    
    @Override
    public void v(String format, Object... args) {
        if(shouldPrint(LogLevel.Verbose)) {
        	mStdLogger.verbose(format, args);
        }
    }
    
    @Override
    public void i(String format, Object... args) {
        if(shouldPrint(LogLevel.Info)) {
        	mStdLogger.info(format, args);
        }
    }
    
    @Override
    public void w(String format, Object... args) {
        if(shouldPrint(LogLevel.Warning)) {
        	mStdLogger.warning(format, args);
        }
    }
    
    @Override
    public void e(Throwable ex, String format, Object... args) {
        if(shouldPrint(LogLevel.Error)) {
            if(ex != null) {
            	mStdLogger.error(null, ex.getMessage(), args);
            }
            mStdLogger.error(null, format, args);
        }
    };
}
