package com.robomorphine.test.filtering;

import com.robomorphine.test.annotation.DisabledTest;
import com.robomorphine.test.annotation.EnabledTest;
import com.robomorphine.test.annotation.LongTest;
import com.robomorphine.test.annotation.ManualTest;
import com.robomorphine.test.annotation.NonUiTest;
import com.robomorphine.test.annotation.PerformanceTest;
import com.robomorphine.test.annotation.ShortTest;
import com.robomorphine.test.annotation.StabilityTest;
import com.robomorphine.test.annotation.UiTest;

import android.test.suitebuilder.TestMethod;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class FilterPredicateTest extends TestCase
{
    private final static String TAG = FilterPredicateTest.class.getSimpleName();
    
    @Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	public static @interface a {}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	public static @interface b {}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	public static @interface c {}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	public static @interface d {}
	
	private static class CustomFilterPredicate extends FilterPredicate
	{
		private final static Map<String,String> ALIAS_MAP;
		static
		{
			Map<String,String> map = new HashMap<String, String>();
			map.put("a", a.class.getName());
			map.put("b", b.class.getName());
			map.put("c", c.class.getName());
			map.put("d", d.class.getName());
			ALIAS_MAP = Collections.unmodifiableMap(map);
		}
		
		public CustomFilterPredicate(String filter)
		{
			super(filter);
		}
		
		@Override
		public String getByAlias(String val)
		{
			if(ALIAS_MAP.containsKey(val))
			{
				return ALIAS_MAP.get(val);
			}			
			return super.getByAlias(val);
		}
	}
	
	@a
	static class ExampleTestCase extends TestCase
	{	
		public void testA() {} //NOPMD
		public @b void testAB() {} //NOPMD
		public @b @c void testABC() {} //NOPMD
		public @b @c @d void testABCD() {} //NOPMD
	}
	
	private final static TestMethod TM_A = new TestMethod("testA", ExampleTestCase.class);
	private final static TestMethod TM_AB = new TestMethod("testAB", ExampleTestCase.class);
	private final static TestMethod TM_ABC = new TestMethod("testABC", ExampleTestCase.class);
	private final static TestMethod TM_ABCD = new TestMethod("testABCD", ExampleTestCase.class);
	
	
	public void testFilter_ordinaryAnnotations()
	{
		FilterPredicate predicate = new CustomFilterPredicate(null);
		assertTrue(predicate.apply(TM_A));
		assertTrue(predicate.apply(TM_AB));
		assertTrue(predicate.apply(TM_ABC));
		assertTrue(predicate.apply(TM_ABCD));
				
		predicate = new CustomFilterPredicate("");
		assertTrue(predicate.apply(TM_A));
		assertTrue(predicate.apply(TM_AB));
		assertTrue(predicate.apply(TM_ABC));
		assertTrue(predicate.apply(TM_ABCD));
		
		predicate = new CustomFilterPredicate("a");
		assertTrue(predicate.apply(TM_A));
		assertTrue(predicate.apply(TM_AB));
		assertTrue(predicate.apply(TM_ABC));
		assertTrue(predicate.apply(TM_ABCD));
		
		predicate = new CustomFilterPredicate("+a");
		assertTrue(predicate.apply(TM_A));
		assertTrue(predicate.apply(TM_AB));
		assertTrue(predicate.apply(TM_ABC));
		assertTrue(predicate.apply(TM_ABCD));
		
		predicate = new CustomFilterPredicate("-a");
		assertFalse(predicate.apply(TM_A));
		assertFalse(predicate.apply(TM_AB));
		assertFalse(predicate.apply(TM_ABC));
		assertFalse(predicate.apply(TM_ABCD));
		
		predicate = new CustomFilterPredicate("+b");
		assertFalse(predicate.apply(TM_A));
		assertTrue(predicate.apply(TM_AB));
		assertTrue(predicate.apply(TM_ABC));
		assertTrue(predicate.apply(TM_ABCD));
		
		predicate = new CustomFilterPredicate("-b");
		assertTrue(predicate.apply(TM_A));
		assertFalse(predicate.apply(TM_AB));
		assertFalse(predicate.apply(TM_ABC));
		assertFalse(predicate.apply(TM_ABCD));
		
		predicate = new CustomFilterPredicate("+c");
		assertFalse(predicate.apply(TM_A));
		assertFalse(predicate.apply(TM_AB));
		assertTrue(predicate.apply(TM_ABC));
		assertTrue(predicate.apply(TM_ABCD));
		
		predicate = new CustomFilterPredicate("+d");
		assertFalse(predicate.apply(TM_A));
		assertFalse(predicate.apply(TM_AB));
		assertFalse(predicate.apply(TM_ABC));
		assertTrue(predicate.apply(TM_ABCD));
		
		predicate = new CustomFilterPredicate("+b-d");
		assertFalse(predicate.apply(TM_A));
		assertTrue(predicate.apply(TM_AB));
		assertTrue(predicate.apply(TM_ABC));
		assertFalse(predicate.apply(TM_ABCD));
		
		predicate = new CustomFilterPredicate("-d+b");
		assertFalse(predicate.apply(TM_A));
		assertTrue(predicate.apply(TM_AB));
		assertTrue(predicate.apply(TM_ABC));
		assertFalse(predicate.apply(TM_ABCD));		
		
		predicate = new CustomFilterPredicate("+d-a");
		assertFalse(predicate.apply(TM_A));
		assertFalse(predicate.apply(TM_AB));
		assertFalse(predicate.apply(TM_ABC));
		assertFalse(predicate.apply(TM_ABCD));
		
		predicate = new CustomFilterPredicate("-d+a");
		assertTrue(predicate.apply(TM_A));
		assertTrue(predicate.apply(TM_AB));
		assertTrue(predicate.apply(TM_ABC));
		assertFalse(predicate.apply(TM_ABCD));
	}
		
	static class AliasExampleTestCase extends TestCase
	{
		public void testNoAnnotationsTestMethod(){} //NOPMD
		
		@android.test.suitebuilder.annotation.SmallTest public void testAndroidSmallTestMethod(){} //NOPMD
		@SmallTest 	public void testSmallTestMethod(){} //NOPMD
		@ShortTest    public void testShortTestMethod(){} //NOPMD
		@android.test.suitebuilder.annotation.MediumTest public void testAndroidMediumTestMethod(){} //NOPMD
		@MediumTest public void testMediumTestMethod(){} //NOPMD
		@android.test.suitebuilder.annotation.LargeTest public void testAndroidLargeTestMethod(){} //NOPMD
		@LargeTest 	public void testLargeTestMethod(){} //NOPMD
		@LongTest    public void testLongTestMethod(){} //NOPMD
		@PerformanceTest public void testPerformanceTestMethod(){} //NOPMD		
		@StabilityTest public void testStabilityTestMethod(){} //NOPMD
		@ManualTest	public void testManualTestMethod(){} //NOPMD
		@UiTest   public void testUiTestMethod(){} //NOPMD
		@NonUiTest   public void testNonUiTestMethod(){} //NOPMD
	}
	
	private final static TestMethod TM_androidSmall = new TestMethod("testAndroidSmallTestMethod", AliasExampleTestCase.class);
	private final static TestMethod TM_small = new TestMethod("testSmallTestMethod", AliasExampleTestCase.class);
	private final static TestMethod TM_short = new TestMethod("testShortTestMethod", AliasExampleTestCase.class);
	private final static TestMethod TM_androidMedium = new TestMethod("testAndroidMediumTestMethod", AliasExampleTestCase.class);
	private final static TestMethod TM_medium = new TestMethod("testMediumTestMethod", AliasExampleTestCase.class);
	private final static TestMethod TM_androidLarge = new TestMethod("testAndroidLargeTestMethod", AliasExampleTestCase.class);
	private final static TestMethod TM_large = new TestMethod("testLargeTestMethod", AliasExampleTestCase.class);
	private final static TestMethod TM_long = new TestMethod("testLongTestMethod", AliasExampleTestCase.class);
	private final static TestMethod TM_perf = new TestMethod("testPerformanceTestMethod", AliasExampleTestCase.class);
	private final static TestMethod TM_stability = new TestMethod("testStabilityTestMethod", AliasExampleTestCase.class);
	private final static TestMethod TM_manual = new TestMethod("testManualTestMethod", AliasExampleTestCase.class);
	
	private final static TestMethod TM_ui= new TestMethod("testUiTestMethod", AliasExampleTestCase.class);
	private final static TestMethod TM_nonui = new TestMethod("testNonuiTestMethod", AliasExampleTestCase.class);
	
	public void testFilter_aliases_small()
	{
	    String [] smallFilters = new String [] { "+s", "+S", "+small", "+short" };
	    for(String filter : smallFilters) {	    
	        Log.i(TAG, "Testing filter: " + filter); //NOPMD
    		FilterPredicate predicate = new FilterPredicate(filter);
    		assertTrue(predicate.apply(TM_androidSmall));
    		assertTrue(predicate.apply(TM_small));
    		assertTrue(predicate.apply(TM_short));
    		assertFalse(predicate.apply(TM_androidMedium));
    		assertFalse(predicate.apply(TM_medium));
    		assertFalse(predicate.apply(TM_androidLarge));
    		assertFalse(predicate.apply(TM_large));
    		assertFalse(predicate.apply(TM_long));
    		assertFalse(predicate.apply(TM_perf));
    		assertFalse(predicate.apply(TM_stability));
    		assertFalse(predicate.apply(TM_manual));
    		
    		//if none of test types specified, "small" is used as default test type
    		assertTrue(predicate.apply(TM_ui)); 
    		assertTrue(predicate.apply(TM_nonui));
	    }
	}
	
	public void testFilter_aliases_medium()
    {
	    String [] mediumFilters = new String [] { "+m", "+M", "+medium" };
        for(String filter : mediumFilters) {
            Log.i(TAG, "Testing filter: " + filter);
            FilterPredicate predicate = new FilterPredicate(filter);
                
            assertFalse(predicate.apply(TM_androidSmall));
            assertFalse(predicate.apply(TM_small));
            assertFalse(predicate.apply(TM_short));
            assertTrue(predicate.apply(TM_androidMedium));
            assertTrue(predicate.apply(TM_medium));
            assertFalse(predicate.apply(TM_androidLarge));
            assertFalse(predicate.apply(TM_large));
            assertFalse(predicate.apply(TM_long));
            assertFalse(predicate.apply(TM_perf));
            assertFalse(predicate.apply(TM_stability));
            assertFalse(predicate.apply(TM_manual));
            
            //if none of test types specified, "small" is used as default test type
            assertFalse(predicate.apply(TM_ui)); 
            assertFalse(predicate.apply(TM_nonui));
        }
    }
	
	public void testFilter_aliases_large()
    {
        String [] largeFilters = new String [] { "+l", "+L", "+large", "+long" };
        for(String filter : largeFilters) {
            Log.i(TAG, "Testing filter: " + filter);
            FilterPredicate predicate = new FilterPredicate(filter);
            
            assertFalse(predicate.apply(TM_androidSmall));
            assertFalse(predicate.apply(TM_small));
            assertFalse(predicate.apply(TM_short));
            assertFalse(predicate.apply(TM_androidMedium));
            assertFalse(predicate.apply(TM_medium));
            assertTrue(predicate.apply(TM_androidLarge));
            assertTrue(predicate.apply(TM_large));
            assertTrue(predicate.apply(TM_long));
            assertFalse(predicate.apply(TM_perf));
            assertFalse(predicate.apply(TM_stability));
            assertFalse(predicate.apply(TM_manual));
            
            //if none of test types specified, "small" is used as default test type
            assertFalse(predicate.apply(TM_ui)); 
            assertFalse(predicate.apply(TM_nonui));
        }
    }
	
	public void testFilter_aliases_perf()
    {
        String [] perfFilters = new String [] { "+p", "+perf", "+performance"};
        for(String filter : perfFilters) {
            Log.i(TAG, "Testing filter: " + filter);
            FilterPredicate predicate = new FilterPredicate(filter);
            
            assertFalse(predicate.apply(TM_androidSmall));
            assertFalse(predicate.apply(TM_small));
            assertFalse(predicate.apply(TM_short));
            assertFalse(predicate.apply(TM_androidMedium));
            assertFalse(predicate.apply(TM_medium));
            assertFalse(predicate.apply(TM_androidLarge));
            assertFalse(predicate.apply(TM_large));
            assertFalse(predicate.apply(TM_long));
            assertTrue(predicate.apply(TM_perf));
            assertFalse(predicate.apply(TM_stability));
            assertFalse(predicate.apply(TM_manual));
            
            //if none of test types specified, "small" is used as default test type
            assertFalse(predicate.apply(TM_ui)); 
            assertFalse(predicate.apply(TM_nonui));
        }
    }
	
	public void testFilter_aliases_stability()
    {
        String [] stabilityFilters = new String [] { "+st", "+stability"};
        for(String filter : stabilityFilters) {
            Log.i(TAG, "Testing filter: " + filter);
            FilterPredicate predicate = new FilterPredicate(filter);
            
            assertFalse(predicate.apply(TM_androidSmall));
            assertFalse(predicate.apply(TM_small));
            assertFalse(predicate.apply(TM_short));
            assertFalse(predicate.apply(TM_androidMedium));
            assertFalse(predicate.apply(TM_medium));
            assertFalse(predicate.apply(TM_androidLarge));
            assertFalse(predicate.apply(TM_large));
            assertFalse(predicate.apply(TM_long));
            assertFalse(predicate.apply(TM_perf));
            assertTrue(predicate.apply(TM_stability));
            assertFalse(predicate.apply(TM_manual));
            
            //if none of test types specified, "small" is used as default test type
            assertFalse(predicate.apply(TM_ui)); 
            assertFalse(predicate.apply(TM_nonui));
        }
    }
	
	public void testFilter_aliases_manual()
    {
        String [] manualFilters = new String [] { "+mn", "+manual"};
        for(String filter : manualFilters) {
            Log.i(TAG, "Testing filter: " + filter);
            FilterPredicate predicate = new FilterPredicate(filter);
            
            assertFalse(predicate.apply(TM_androidSmall));
            assertFalse(predicate.apply(TM_small));
            assertFalse(predicate.apply(TM_short));
            assertFalse(predicate.apply(TM_androidMedium));
            assertFalse(predicate.apply(TM_medium));
            assertFalse(predicate.apply(TM_androidLarge));
            assertFalse(predicate.apply(TM_large));
            assertFalse(predicate.apply(TM_long));
            assertFalse(predicate.apply(TM_perf));
            assertFalse(predicate.apply(TM_stability));
            assertTrue(predicate.apply(TM_manual));
            
            //if none of test types specified, "small" is used as default test type
            assertFalse(predicate.apply(TM_ui)); 
            assertFalse(predicate.apply(TM_nonui));
        }
        
    }
	
	public void testFilter_aliases_ui()
    {
        String [] uiFilters = new String [] { "+ui"};
        for(String filter : uiFilters) {
            Log.i(TAG, "Testing filter: " + filter);
            FilterPredicate predicate = new FilterPredicate(filter);
            
            assertFalse(predicate.apply(TM_androidSmall));
            assertFalse(predicate.apply(TM_small));
            assertFalse(predicate.apply(TM_short));
            assertFalse(predicate.apply(TM_androidMedium));
            assertFalse(predicate.apply(TM_medium));
            assertFalse(predicate.apply(TM_androidLarge));
            assertFalse(predicate.apply(TM_large));
            assertFalse(predicate.apply(TM_long));
            assertFalse(predicate.apply(TM_perf));
            assertFalse(predicate.apply(TM_stability));
            assertFalse(predicate.apply(TM_manual));
            assertTrue(predicate.apply(TM_ui)); 
            assertFalse(predicate.apply(TM_nonui));
        }
        
    }
	
	public void testFilter_aliases_nonui()
    {
        String [] nonuiFilters = new String [] { "-ui"};
        for(String filter : nonuiFilters) {
            Log.i(TAG, "Testing filter: " + filter);
            FilterPredicate predicate = new FilterPredicate(filter);
            
            assertTrue(predicate.apply(TM_androidSmall));
            assertTrue(predicate.apply(TM_small));
            assertTrue(predicate.apply(TM_short));
            assertTrue(predicate.apply(TM_androidMedium));
            assertTrue(predicate.apply(TM_medium));
            assertTrue(predicate.apply(TM_androidLarge));
            assertTrue(predicate.apply(TM_large));
            assertTrue(predicate.apply(TM_long));
            assertTrue(predicate.apply(TM_perf));
            assertTrue(predicate.apply(TM_stability));
            assertTrue(predicate.apply(TM_manual));
            assertFalse(predicate.apply(TM_ui)); 
            assertTrue(predicate.apply(TM_nonui));
        }   
		
    }
	
	public void testFilter_aliases_custom()
    {
		FilterPredicate predicate = new FilterPredicate("+s-m+l-p+st-mn-ui");
		assertTrue(predicate.apply(TM_androidSmall));
        assertTrue(predicate.apply(TM_small));
        assertTrue(predicate.apply(TM_short));
        assertFalse(predicate.apply(TM_androidMedium));
        assertFalse(predicate.apply(TM_medium));
        assertTrue(predicate.apply(TM_androidLarge));
        assertTrue(predicate.apply(TM_large));
        assertTrue(predicate.apply(TM_long));
        assertFalse(predicate.apply(TM_perf));
        assertTrue(predicate.apply(TM_stability));
        assertFalse(predicate.apply(TM_manual));
        assertFalse(predicate.apply(TM_ui)); 
        assertTrue(predicate.apply(TM_nonui));
	}
	
	static class MethodDisabledTestCase extends TestCase
    {   
        @DisabledTest("For test reasons") public void testDisabledTestMethod(){} //NOPMD
        @EnabledTest public void testEnabledTestMethod(){} //NOPMD
        public void testMethod(){} //NOPMD
    }
    
    @DisabledTest("For test reasons") 
    static class ClassDisabledTestCase extends TestCase
    {   
        @DisabledTest("For test reasons") public void testDisabledTestMethod(){} //NOPMD
        @EnabledTest public void testEnabledTestMethod(){} //NOPMD
        public void testMethod(){} //NOPMD
    }
	
	public void testFilter_disabled_method()
    {	    
        FilterPredicate predicate = new FilterPredicate("");
        
        TestMethod disabled = new TestMethod("testDisabledTestMethod", MethodDisabledTestCase.class);
        TestMethod enabled = new TestMethod("testEnabledTestMethod", MethodDisabledTestCase.class);
        TestMethod none = new TestMethod("testMethod", MethodDisabledTestCase.class);
        assertFalse(predicate.apply(disabled));
        assertTrue(predicate.apply(enabled));
        assertTrue(predicate.apply(none));
    }
	
	public void testFilter_disabled_class()
    {       
        FilterPredicate predicate = new FilterPredicate("");
        
        TestMethod disabled = new TestMethod("testDisabledTestMethod", ClassDisabledTestCase.class);
        TestMethod enabled = new TestMethod("testEnabledTestMethod", ClassDisabledTestCase.class);
        TestMethod none = new TestMethod("testMethod", ClassDisabledTestCase.class);
        assertFalse(predicate.apply(disabled));
        assertTrue(predicate.apply(enabled));
        assertFalse(predicate.apply(none));
    }
}
