package com.robomorphine.test.log;

public class NullLog implements ILog {
    
    @Override
    public void v(String format, Object... args) {        
    }
        
    @Override
    public void i(String format, Object... args) {
    }
    
    @Override
    public void w(String format, Object... args) {
    }
    
    @Override
    public void e(Throwable ex, String format, Object... args) {
    }
    
}
