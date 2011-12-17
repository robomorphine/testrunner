package com.robomorphine.test.ant;

import java.util.HashMap;

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
        
        public void verify(CreateAvdTask task) {
            if(mKey == null || mKey.length() == 0 || mValue == null) {
                task.error("Hardware argument \"%s\" must have both \"key\" and \"value\" speicifed.",
                            mKey, mValue);
            }
        }
    } 
    
    public static class Arg extends ArgBase {
        @Override
        public void setKey(String key) {
            super.setKey(key);
        }
        
        @Override
        public void setValue(String value) {
            super.setValue(value);
        }
    }
    
    public static class RamSize extends ArgBase {
        public RamSize() {
            setKey("hw.ramSize");
        }
        
        public void setSize(int size) {
            setValue(Integer.toString(size));
        }
    }
    
    public static class HeapSize extends ArgBase {
        public HeapSize() {
            setKey("vm.heapSize");
        }
        
        public void setSize(int size) {
            setValue(Integer.toString(size));
        }
    }
    
    private final CreateAvdTask mCreateAvdTask;
    private final HashMap<String, String> mHardware;
    public AvdHardware(CreateAvdTask task, HashMap<String, String> hw) {
        mCreateAvdTask = task;
        mHardware = hw;
    }
    
    public void addConfiguredArg(Arg arg) {
        arg.verify(mCreateAvdTask);
        mHardware.put(arg.getKey(), arg.getValue());
    }
    
    public void addConfiguredRam(RamSize arg) {
        arg.verify(mCreateAvdTask);
        mHardware.put(arg.getKey(), arg.getValue());
    }
    
    public void addConfiguredHeap(HeapSize arg) {
        arg.verify(mCreateAvdTask);
        mHardware.put(arg.getKey(), arg.getValue());
    }
}
