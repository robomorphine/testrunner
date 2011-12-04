package com.robomorphine.test.log;

public interface ILog {
        
    void info(String format, Object...args);
    void warning(String format, Object...args);
    void error(Throwable ex, String format, Object...args);
}
