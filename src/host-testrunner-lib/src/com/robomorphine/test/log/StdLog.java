package com.robomorphine.test.log;

import com.android.sdklib.StdSdkLog;

public class StdLog implements ILog  {
    
    private StdSdkLog mStdLog = new StdSdkLog();
    
    @Override
    public void info(String format, Object... args) {
        mStdLog.printf(format, args);
    }
    
    @Override
    public void warning(String format, Object... args) {
        mStdLog.warning(format, args);
    }
    
    @Override
    public void error(Throwable ex, String format, Object... args) {
        if(ex != null) {
            mStdLog.error(null, ex.getMessage(), args);
        }
        mStdLog.error(null, format, args);
    };
}
