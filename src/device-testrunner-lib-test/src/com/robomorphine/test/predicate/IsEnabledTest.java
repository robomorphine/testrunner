package com.robomorphine.test.predicate;

import com.robomorphine.test.annotation.DisabledTest;
import com.robomorphine.test.annotation.EnabledTest;

import android.test.suitebuilder.TestMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

public class IsEnabledTest extends TestCase {
    
    private static final String DISABLED_REASON = "test"; 
    
    @Retention(RetentionPolicy.RUNTIME)
    static @interface ExpectedResult {
        boolean value();
    }
    
    static class NoClassAnnotations extends TestCase {
        @ExpectedResult(true)
        public void testNoMethodAnnotations() { // NOPMD
        } 

        @ExpectedResult(true)
        @EnabledTest
        public void testEnabled() { // NOPMD
        } 

        @ExpectedResult(false)
        @DisabledTest(DISABLED_REASON)
        public void testDisabled() {  // NOPMD
        }        

        @ExpectedResult(false)
        @EnabledTest
        @DisabledTest(DISABLED_REASON)
        public void testEnabledAndDisabled() { //NOPMD
        }
    }
    
    @EnabledTest
    static class EnabledClass extends TestCase {
        @ExpectedResult(true)
        public void noMethodAnnotations() {
        }
        
        @ExpectedResult(true)
        @EnabledTest
        public void enabled() {            
        }
        
        @ExpectedResult(false)
        @DisabledTest(DISABLED_REASON)
        public void  disabled() {            
        }
        
        @ExpectedResult(false)
        @EnabledTest @DisabledTest(DISABLED_REASON)
        public void enabledAndDisabled(){            
        }
    }
    
    @DisabledTest(DISABLED_REASON)
    static class DisabledClass extends TestCase {
        @ExpectedResult(false)
        public void noMethodAnnotations() {
        }
        
        @ExpectedResult(true)
        @EnabledTest
        public void enabled() {            
        }
        
        @ExpectedResult(false)
        @DisabledTest(DISABLED_REASON)
        public void  disabled() {            
        }
        
        
        @ExpectedResult(false)
        @EnabledTest @DisabledTest(DISABLED_REASON)
        public void enabledAndDisabled(){            
        }
    }
    
    @EnabledTest @DisabledTest(DISABLED_REASON)
    static class EnabledDisabledClass extends TestCase {
        @ExpectedResult(false)
        public void noMethodAnnotations() {
        }
        
        @ExpectedResult(true)
        @EnabledTest
        public void enabled() {            
        }
        
        @ExpectedResult(false)
        @DisabledTest(DISABLED_REASON)
        public void  disabled() {            
        }
        
        @ExpectedResult(false)
        @EnabledTest @DisabledTest(DISABLED_REASON)
        public void enabledAndDisabled(){            
        }
    }
    
    public void testAnnotations() {
        List<Class<? extends TestCase>> testCases = new LinkedList<Class<? extends TestCase>>();
        IsEnabled predicate = new IsEnabled();
        
        testCases.add(NoClassAnnotations.class);
        testCases.add(EnabledClass.class);
        testCases.add(DisabledClass.class);
        testCases.add(EnabledDisabledClass.class);        
        
        for(Class<? extends TestCase> clazz : testCases) {            
            for(Method method : clazz.getDeclaredMethods()) {                
                TestMethod testMethod = new TestMethod(method.getName(), clazz);
                
                ExpectedResult expectedAnnotaiton = method.getAnnotation(ExpectedResult.class);
                assertNotNull("Has no expected result: " + method, expectedAnnotaiton);
                
                boolean expected = expectedAnnotaiton.value();
                boolean actual = predicate.apply(testMethod);
                
                String name = testMethod.getEnclosingClass().getSimpleName() + "." + testMethod.getName();
                assertEquals(name, expected, actual);
            }
        }
    }
    
    private final static String IGNORED_REASON = "ignored-reason";
    private final static String CLASS_REASON = "class-reason";
    private final static String METHOD_REASON = "method-reason";
    
    static class ReasonEnabledClass extends TestCase {
        public void testMethod() {} //NOPMD
    }
    
    @DisabledTest(IGNORED_REASON)
    static class ReasonEnabledMethod extends TestCase {
        @EnabledTest public void testMethod() {} //NOPMD
    }
    
    @DisabledTest(CLASS_REASON)
    static class ReasonDisabledClass extends TestCase {        
        public void testMethod() {} //NOPMD
    }   
    
    static class ReasonDisabledMethod extends TestCase {
        @DisabledTest(METHOD_REASON)
        public void testMethod() {} //NOPMD
    }
    
    public void testDisabledReason() {
        String testMethod = "testMethod";
        
        TestMethod method = new TestMethod(testMethod, ReasonEnabledClass.class);        
        String reason = IsEnabled.getDisabledReason(method);
        assertNull(reason);
        
        method = new TestMethod(testMethod, ReasonEnabledMethod.class);
        reason = IsEnabled.getDisabledReason(method);
        assertNull(reason);
        
        method = new TestMethod(testMethod, ReasonDisabledClass.class);
        reason = IsEnabled.getDisabledReason(method);
        assertEquals(CLASS_REASON, reason);
        
        method = new TestMethod(testMethod, ReasonDisabledMethod.class);
        reason = IsEnabled.getDisabledReason(method);
        assertEquals(METHOD_REASON, reason);
    }
}
