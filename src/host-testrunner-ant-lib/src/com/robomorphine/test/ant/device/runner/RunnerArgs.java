package com.robomorphine.test.ant.device.runner;

import com.robomorphine.test.ant.BaseTask;

import org.apache.tools.ant.BuildException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RunnerArgs {
    
    public static class BaseArg {
        
        private String mKey;
        private String mValue;
        
        public boolean allowMultiple() {
            return true;
        }
        
        protected void setKey(String key) {
            mKey = key;        
        }
        
        public String getKey() {
            return mKey;
        }
        
        protected void setValue(String value) {
            mValue = value;
        }
        
        public String getValue() {
            return mValue;
        }
        
        public void verify(BaseTask task) {
            if(mKey == null) {
                task.error("TestRunner arguments must have key specified.");
            }
            
            if(mValue == null) {
                task.error("TestRunner arguments must have value specified.");
            }
        }
    }
    
    public static class Arg extends BaseArg {
        @Override
        public void setKey(String key) {
            super.setKey(key);
        }
        
        @Override
        public void setValue(String value) {
            super.setValue(value);
        }
    }
    
    public static class FixedKeyArg extends BaseArg {
                
        private final String mAttributeName;
        public FixedKeyArg(String key, String attributeName) {
            setKey(key);
            mAttributeName = attributeName;
        }
        
        @Override
        public void verify(BaseTask task) {
            if(getValue() == null) {
                task.error("Required attrbiute is missing: %s", mAttributeName);
            }
            super.verify(task);
        }
    }
     
    public static class EnableArg extends FixedKeyArg {
        public EnableArg(String key) {
            super("key", "enable");
        }
        
        public boolean allowMultiple() {
            return false;
        }
        
        public void setEnable(boolean enable) {
            setValue(Boolean.toString(enable));
        }
    }
    
    public static class CoverageArg extends EnableArg {
        public CoverageArg() {
            super("coverage");
        }
    }
    
    public static class LogOnlyArg extends EnableArg {
        public LogOnlyArg() {
            super("log");
        }
    }
    
    public static class DebugArg extends EnableArg {
        public DebugArg() {
            super("debug");
        }
    } 
    
    public static class TestClassArg extends FixedKeyArg {
        public static class ClassName {
            private String mName;
            public void setName(String name) {
                mName = name;
            }
            
            public String getName() {
                return mName;
            }
        }
        
        private List<String> mNames = new LinkedList<String>();        
        public TestClassArg() {
            super("class", "name");
        }
        
        public void setName(String name) {
            setValue(name);
        }
        
        public void setNames(List<String> names) {
            StringBuilder builder = new StringBuilder();
            for(String name : names) {
                if(builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(name);
            }
            setValue(builder.toString());
        }
        
        public void addConfiguredName(ClassName className) {
            if(className.getName() == null) {
                throw new BuildException("Class name attribute not specified");
            }
            mNames.add(className.getName());
            setNames(mNames);
        }
    }
    
    private final BaseTask mTask;
    private final Set<Class<?>> mExistingArgType = new HashSet<Class<?>>();
    private final LinkedHashMap<String, String> mArgs = new LinkedHashMap<String, String>();
    public RunnerArgs(BaseTask task) {
        mTask = task;
    }
    
    private void addBaseArg(BaseArg arg) {
        if(!arg.allowMultiple()) {
            if(mExistingArgType.contains(arg.getClass())) {
                mTask.error("Mutltiple entries of %s are not allowed.", arg.getClass().getSimpleName());
            }
            mExistingArgType.add(arg.getClass());
        }
        arg.verify(mTask);
        mArgs.put(arg.getKey(), arg.getValue());
    }
    
    public void addConfiguredArg(Arg arg) {
        addBaseArg(arg);
    }
    
    public void addConfiguredCoverage(CoverageArg arg) {
        addBaseArg(arg);
    }
    
    public void addConfiguredLogOnly(LogOnlyArg arg) {
        addBaseArg(arg);
    }
    
    public void addConfiguredDebug(DebugArg arg) {
        addBaseArg(arg);
    }
    
    public void addConfiguredClass(TestClassArg arg) {
        addBaseArg(arg);
    }
    
    public HashMap<String, String> getArgs() {
        return mArgs;
    }

}
