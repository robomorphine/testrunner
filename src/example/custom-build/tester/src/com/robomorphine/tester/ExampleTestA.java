package com.robomorphine.tester;

import junit.framework.TestCase;

public class ExampleTestA extends TestCase {
    
    public void testA() {
        
    }
    
    public void testB() {
        
    }
    
    public void testFailed() {
        fail("failed");
    }
    
    public void testError() {
        throw new IllegalStateException("error");
    }

}
