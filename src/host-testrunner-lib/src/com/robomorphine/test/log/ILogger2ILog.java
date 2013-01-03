package com.robomorphine.test.log;

import com.android.annotations.NonNull;
import com.android.utils.ILogger;

public class ILogger2ILog implements ILogger {
    
    private final ILog mLogger;
    
    public ILogger2ILog(ILog logger) {
        mLogger = logger;
    }
    
    public void printf(@NonNull String format, Object... args) {
        mLogger.v(format, args);        
    }
    
    @Override
    public void verbose(@NonNull String format, Object... args) {
    	mLogger.v(format, args);
    }
    
    @Override
    public void info(@NonNull String format, Object... args) {
    	mLogger.i(format, args);
    }
    
    @Override
    public void warning(@NonNull String format, Object... args) {
        mLogger.w(format, args);
    }
    
    @Override
    public void error(Throwable ex, @NonNull String format, Object... args) {
        mLogger.e(ex, format, args);
    }
}
