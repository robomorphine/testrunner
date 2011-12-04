package com.robomorphine.test.log;

public class NullLog implements ILog {
    
    @Override
    public void info(String format, Object... args) {
    }
    
    @Override
    public void warning(String format, Object... args) {
    }
    
    @Override
    public void error(Throwable ex, String format, Object... args) {
    }
}
