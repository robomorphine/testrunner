package com.robomorphine.test.log;

import com.android.sdklib.StdSdkLog;

public class StdLog implements ILog  {
    
    private StdSdkLog mStdLog = new StdSdkLog();
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
            mStdLog.printf(format, args);
        }
    }
    
    @Override
    public void i(String format, Object... args) {
        if(shouldPrint(LogLevel.Info)) {
            mStdLog.printf(format, args);
        }
    }
    
    @Override
    public void w(String format, Object... args) {
        if(shouldPrint(LogLevel.Warning)) {
            mStdLog.warning(format, args);
        }
    }
    
    @Override
    public void e(Throwable ex, String format, Object... args) {
        if(shouldPrint(LogLevel.Error)) {
            if(ex != null) {
                mStdLog.error(null, ex.getMessage(), args);
            }
            mStdLog.error(null, format, args);
        }
    };
}
