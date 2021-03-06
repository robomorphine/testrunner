package com.robomorphine.test.filtering;

import com.robomorphine.test.annotation.LongTest;
import com.robomorphine.test.annotation.ManualTest;
import com.robomorphine.test.annotation.NonUiTest;
import com.robomorphine.test.annotation.PerformanceTest;
import com.robomorphine.test.annotation.ShortTest;
import com.robomorphine.test.annotation.StabilityTest;
import com.robomorphine.test.annotation.UiTest;
import com.robomorphine.test.filtering.FilterParser.FilterEntry;
import com.robomorphine.test.predicate.HasAnnotation;
import com.robomorphine.test.predicate.IsEnabled;
import com.robomorphine.test.predicate.IsUiTest;
import com.robomorphine.test.predicate.Predicate;
import com.robomorphine.test.predicate.Predicates;
import com.robomorphine.test.predicate.TestTypeEqualsTo;

import android.test.suitebuilder.TestMethod;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This predicate has special logic for filtering test cases. 
 * As input it receives a filter of type: "[{+,-}<annotationClass>]*", 
 * where "+" symbol means "include" and "-" means "exclude".
 *  
 *         For example:    "+andoird.test.ann1+andoird.test.ann2-andoird.test.ann3".
 *                         "-andoird.test.ann1"
 *                         "-andoird.test.ann1-andoird.test.ann2"
 * 
 * For any TestMethod to be included it should have ANY of "+" annotations and NONE 
 * of the "-" annotations.
 * ( Exclusion "-" takes precedences over Inclusion "+" ). The order is not relevant.
 * 
 * Note: TestMethod annotations are "inherited" from enclosing TestCase class.
 * 
 *      For example:
 *          @a
 *          class T1 extends TestCase
 *          {
 *              void tm1() {}
 *              @b void tm2() {}
 *              @b @c void tm3() {}
 *              @b @c @d void tm4() {}
 *          }
 *  
 *          "+a" -> tm1, tm2, tm3, tm4
 *          "-a" -> none
 *          "+b" -> tm2, tm3, tm4
 *          "-b" -> tm1
 *          "+a-b" -> tm1
 *          "+c-d" -> tm3
 *          "-d+a" -> tm1, tm2, tm3
 * 
 * 
 * As a special case, if first +/- symbol is omitted then predicate assumes "+".
 * 
 *    For example:  "android.test.ann1+android.test.ann2" == "+android.test.ann1+android.test.ann2".
 * 
 * Some specific annotations have built-in aliases. This aliases are case insensitive 
 * (but note that annotation class names _are_ case sensitive). List of aliases: 
 * 
 * s, small - com.robomorphine.test.annotation.SmallTest
 * short    - com.robomorphine.test.annotation.ShortTest (same as SmallTest) 
 * m, medium  - com.robomorphine.test.annotation.MediumTest
 * l, large   - com.robomorphine.test.annotation.LargeTest
 * long       - com.robomorphine.test.annotation.LongTest (same as LargeTest) * 
 * p, perf, performance - com.robomorphine.test.annotation.PerformanceTest
 * st, stability - com.robomorphine.test.annotation.StabilityTest
 * mn, manual - com.robomorphine.test.annotation.ManualTest
 * ui - com.robomorphine.test.annotation.UiTest
 * 
 *         For example: 
 * 
 *         "+s-medium" == "+com.robomorphine.test.annotation.SmallTest-com.robomorphine.test.annotation.MediumTest"
 *         "s+M+l" == "s+m+l" == "S+M+L"
 *    
 * But some annotations have special meaning and a handled in a different way then all other annotations are.
 * 
 * First special annotation is "com.robomorphine.test.annotation.DisabledTest". When test 
 * is marked with such annotation it will not run. 
 * 
 * Second group of special annotation are:
 * 
 *  UiTest - com.robomorphine.test.annotation.UiTest
 *  NonUiTest - com.robomorphine.test.annotation.NonUiTest
 *  
 * Note that when "+ui" or "-ui" is specified on filter, FilterPredicate uses special logic for 
 * filtering. It does not use usual "annotation present" logic. If "+ui" or "-ui" is met in filter,
 * than "IsUiTest" predicate is used on all test methods. It has next logic (in order of precedence):
 *  
 *  1) If test method has NonUiTest annotation, its considered non-ui test.
 *  2) If test method has UiTest annotation, its considered ui test.
 *  3) If test method's class has NonUiTest annotation, its considered non-ui test.
 *  4) If test method's class has UiTest annotation, its considered ui test.
 *  5) If test method's class extends ActivityInstrumentationTestCase/mIsActivityInstrumentationTestCase2
 *     class its considered ui test.
 *  6) Otherwise test method is considered non-ui test.
 *  
 * At any given time each test method is either ui test or non-ui test. If "+ui" test is specified,
 * then only ui tests are filtered in. If "-ui" is specified, then only non-ui tests are filtered in.
 * If "+ui"/"-ui" is omitted from filter then both ui and non-ui tests are filtered in.
 * 
 * Third (and last) group of special annotations represent test types:
 *  
 *   small/short - com.robomorphine.test.annotation.SmallTest/.ShortTest
 *   medium - android.test.suitebuilder.annotation.MediumTest
 *   large/long - android.test.suitebuilder.annotation.LargeTest/.LongTest   
 *   performance - android.test.suitebuilder.annotation.PerformanceTest
 *   stability - android.test.suitebuilder.annotation.StabilityTest
 *   manual - android.test.suitebuilder.annotation.ManualTest
 *   
 * At any given time any TestMethod may have only one effective test type associated with it. 
 * Even if several annotations are applied to this TestMethod, only one will take effect during filtering. 
 * Here is how effective test type is calculated:
 * 
 *  1) If TestMethod has only one TestType annotation -> this annotation takes effect.
 *      For example: 
 *          @SmallTest void testM1() {} -> small test
 *          @ManualTest void testM2() {} -> manual test
 *  
 *  2) If TestMethod has several TestType annotations, only one annotations takes effect. 
 *     The winner is determined by annotation precedence order which is next:
 *     manual > stability > performance > large/long > medium > small/short.
 *     
 *     For example:
 *             @SmallTest @LargeTest void testM1() -> large test
 *             @LargeTest @SmallTest void testM1() -> large test
 *             @LargeTest @SmallTest @ManualTest void testM1() -> manual test
 *  
 *  3) If TestMethod has no test type annotations, then test type annotations are taken 
 *     from enclosing TestCase class. If TestCase class has only one test type annotation
 *     then it takes effects.
 *     
 *      For example: 
 *          @SmallTest class T1 extends TestCase 
 *          {
 *               void testM1() {} -> small test 
 *             } 
 *     
 *             @LargeTest class T1 extends TestCase 
 *          {
 *               void testM1() {} -> large test
 *             }
 *     
 *  4) If TestMethod has no test type annotations, but enclosing TestCase class has 
 *      several test type annotations, then only one test type annotation takes effect.
 *      This annotation is determined as in step #2 for test methods: according to test type precedence.
 *  
 *      For example:
 *  
 *       @SmallTest @LargeTest class T1 extends TestCase 
 *      {
 *          void testM1() {} -> large test 
 *      } 
 *     
 *      @LargeTest @SmallTest class T1 extends TestCase 
 *      {
 *          void testM1() {} -> large test 
 *      }
 *     
 *      @LargeTest @SmallTest @ManualTest class T1 extends TestCase 
 *      {
 *          void testM1() {} -> manual test 
 *      }
 *     
 *  5) If nor TestMethod, neither enclosing TestCase class has any of the test type annotations,
 *      then test is considered to be SmallTest.
 *  
 *      For example:
 *  
 *      class T1 extends TestCase 
 *      {
 *          void testM1() {} -> small test 
 *      }  
 *
 */
public class FilterPredicate implements Predicate<TestMethod> { //NOPMD
    private static final String TAG = FilterPredicate.class.getSimpleName();
    
    private static String toLowerCase(String val) {
        return val.toLowerCase(Locale.ENGLISH);
    }

    private final static Map<String, String> KNOWN_ALIASES;
    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put(toLowerCase("s"), SmallTest.class.getName());
        map.put(toLowerCase("small"), SmallTest.class.getName());
        map.put(toLowerCase("short"), ShortTest.class.getName());
        map.put(toLowerCase("m"), MediumTest.class.getName());
        map.put(toLowerCase("medium"), MediumTest.class.getName());
        map.put(toLowerCase("l"), LargeTest.class.getName());
        map.put(toLowerCase("large"), LargeTest.class.getName());
        map.put(toLowerCase("long"), LongTest.class.getName());
        map.put(toLowerCase("p"), PerformanceTest.class.getName());
        map.put(toLowerCase("perf"), PerformanceTest.class.getName());
        map.put(toLowerCase("performance"), PerformanceTest.class.getName());
        map.put(toLowerCase("st"), StabilityTest.class.getName());
        map.put(toLowerCase("stable"), StabilityTest.class.getName());
        map.put(toLowerCase("stability"), StabilityTest.class.getName());
        map.put(toLowerCase("ui"), UiTest.class.getName());
        map.put(toLowerCase("mn"), ManualTest.class.getName());
        map.put(toLowerCase("manual"), ManualTest.class.getName());
        KNOWN_ALIASES = Collections.unmodifiableMap(map);
    }

    public String getByAlias(String val) {
        val = toLowerCase(val);
        if (KNOWN_ALIASES.containsKey(val)) {
            return KNOWN_ALIASES.get(val);
        }
        return val;
    }   

    private final static Predicate<TestMethod> TRUE_PREDICATE = Predicates.<TestMethod>alwaysTrue();

    private final Predicate<TestMethod> mPredicate;

    public FilterPredicate(String filter) {
        Log.w(TAG, "Filtering tests with: \"" + filter + "\"");
        mPredicate = createPredicate(FilterParser.parse(filter)); //NOPMD
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Annotation> getAnnotationClass(String annotationClassName) {
        if (annotationClassName == null) {
            return null;
        }
        annotationClassName = getByAlias(annotationClassName);

        try {
            Class<?> annotationClass = Class.forName(annotationClassName);
            if (annotationClass.isAnnotation()) {
                return (Class<? extends Annotation>) annotationClass;
            } else {
                String msg = String.format("Provided annotation value \"%s\" is not an Annotation",
                        annotationClassName);
                Log.e(TAG, msg);
            }
        } catch (ClassNotFoundException e) {
            String msg = String.format("Could not find class for specified annotation \"%s\"",
                    annotationClassName);
            Log.e(TAG, msg);
        }
        return null;
    }

    @SuppressWarnings("all")
    private Predicate<TestMethod> createPredicate(List<FilterEntry> filterEntries) {
        if (filterEntries == null) {
            filterEntries = new LinkedList<FilterEntry>();
        }

        List<Predicate<TestMethod>> orPredicates = new ArrayList<Predicate<TestMethod>>(
                filterEntries.size());

        List<Predicate<TestMethod>> andPredicates = new ArrayList<Predicate<TestMethod>>(
                filterEntries.size());

        for (FilterEntry filterEntry : filterEntries) {
            Class<? extends Annotation> annotation = getAnnotationClass(filterEntry.value);
            if (annotation != null) {
                addPredicate(filterEntry.action, annotation, orPredicates, andPredicates);
            }
        }
        
        /* skip disabled tests */
        andPredicates.add(new IsEnabled());

        if (orPredicates.isEmpty())
            orPredicates.add(TRUE_PREDICATE);
        if (andPredicates.isEmpty())
            andPredicates.add(TRUE_PREDICATE);
        return Predicates.and(Predicates.or(orPredicates), Predicates.and(andPredicates));
    }

    private void addPredicate(int action, Class<? extends Annotation> annotation,   //NOPMD
            List<Predicate<TestMethod>> orPredicates, List<Predicate<TestMethod>> andPredicates) { 
        /* Special handling for annotations that indicate test type */
        if (TestTypeEqualsTo.isTestTypeAnnotation(annotation)) {
            if (action == FilterParser.INCLUDE_ACTION) {
                orPredicates.add(new TestTypeEqualsTo(annotation));
            } else if (action == FilterParser.EXCLUDE_ACTION) {
                andPredicates.add(Predicates.<TestMethod> not(new TestTypeEqualsTo(annotation)));
            }
        } else if(annotation == UiTest.class) {
            if(action == FilterParser.INCLUDE_ACTION) {
                andPredicates.add(new IsUiTest());
            } else if(action == FilterParser.EXCLUDE_ACTION) {
                andPredicates.add(Predicates.<TestMethod>not(new IsUiTest()));
            }
        } else if(annotation == NonUiTest.class) {        
            if (action == FilterParser.INCLUDE_ACTION) {
                andPredicates.add(Predicates.<TestMethod>not(new IsUiTest()));
            } else if(action == FilterParser.EXCLUDE_ACTION) {
                andPredicates.add(new IsUiTest());
            }
        } else {
            /* All other annotation are handled using standard rules */
            if(action == FilterParser.INCLUDE_ACTION) {
                orPredicates.add(new HasAnnotation(annotation));
            } else if(action == FilterParser.EXCLUDE_ACTION) {
                andPredicates.add(Predicates.<TestMethod> not(new HasAnnotation(annotation)));
            }
        }
    }

    @Override
    public boolean apply(TestMethod t) {
        return mPredicate.apply(t);
    }
    
    @Override
    public String toString() {
        return "[filter " + mPredicate.toString() + "]";
    }
}
