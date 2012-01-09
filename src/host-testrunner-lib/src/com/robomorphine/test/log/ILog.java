package com.robomorphine.test.log;

public interface ILog {
    
    enum LogLevel { 
        Verbose(1), 
        Debug(2), 
        Info(3), 
        Warning(4), 
        Error(5);
        
        private final int mLevel;
        LogLevel(int level) {
            mLevel = level; 
        }
        
        public int getLevel() {
            return mLevel;
        }
    };
    
        
    void v(String format, Object...args);
    void i(String format, Object...args);
    void w(String format, Object...args);
    void e(Throwable ex, String format, Object...args);
}
