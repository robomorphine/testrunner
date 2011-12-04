package com.robomorphine.test;

import java.util.List;

public interface DeviceManager {
    
    public void startEmulator(String avdName);
    public void stopEmulator(String serial, boolean forceIfNeeded);
    
    public void stopAllEmulators();
    
    public List<String> findDevice(boolean predicate);
}
