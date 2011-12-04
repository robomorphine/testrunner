package com.robomorphine.test.log;

import com.android.sdklib.ISdkLog;

public class ISdkLog2ILog implements ISdkLog {
    
    private final ILog mLogger;
    
    public ISdkLog2ILog(ILog logger) {
        mLogger = logger;
    }
    
    @Override
    public void printf(String format, Object... args) {
        mLogger.info(format, args);        
    }
    
    @Override
    public void warning(String format, Object... args) {
        mLogger.warning(format, args);
    }
    
    @Override
    public void error(Throwable ex, String format, Object... args) {
        mLogger.error(ex, format, args);
    }
}
