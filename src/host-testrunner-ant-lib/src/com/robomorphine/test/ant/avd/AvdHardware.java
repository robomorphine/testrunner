package com.robomorphine.test.ant.avd;

import java.util.Map;

public class AvdHardware {
    
    public static class ArgBase {
        private String mKey;
        private String mValue;
        
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
        
        public void verify(AvdConfigTask task) {
            if(mKey == null || mKey.length() == 0 || mValue == null) {
                task.error("Hardware argument \"%s\" must have both \"key\" and \"value\" speicifed.",
                            mKey, mValue);
            }
        }
    } 
    
    public static class Arg extends ArgBase {
        @Override
        public void setKey(String key) { // NOPMD 
            super.setKey(key);
        }
        
        @Override
        public void setValue(String value) { // NOPMD 
            super.setValue(value);
        }
    }
    
    public static class RamSize extends ArgBase {
        public RamSize() {
            super();
            setKey("hw.ramSize");
        }
        
        public void setSize(int size) {
            setValue(Integer.toString(size));
        }
    }
    
    public static class HeapSize extends ArgBase {
        public HeapSize() {
            super();
            setKey("vm.heapSize");
        }
        
        public void setSize(int size) {
            setValue(Integer.toString(size));
        }
    }
    
    private final AvdConfigTask mAvdTask;
    private final Map<String, String> mHardware;
    public AvdHardware(AvdConfigTask task, Map<String, String> hw) {
        mAvdTask = task;
        mHardware = hw;
    }
    
    public void addConfiguredArg(Arg arg) {
        arg.verify(mAvdTask);
        mHardware.put(arg.getKey(), arg.getValue());
    }
    
    public void addConfiguredRam(RamSize arg) {
        arg.verify(mAvdTask);
        mHardware.put(arg.getKey(), arg.getValue());
    }
    
    public void addConfiguredHeap(HeapSize arg) {
        arg.verify(mAvdTask);
        mHardware.put(arg.getKey(), arg.getValue());
    }
}
