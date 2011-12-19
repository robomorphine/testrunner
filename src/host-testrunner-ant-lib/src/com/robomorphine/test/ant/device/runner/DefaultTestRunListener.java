package com.robomorphine.test.ant.device.runner;

import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.robomorphine.test.ant.BaseTask;

import java.util.Map;

public class DefaultTestRunListener implements ITestRunListener {

    private final BaseTask mTask;
    private int mTotalTestCount;
    private int mFailedTestCount;
    
    private boolean mTestFailed = false;
    private long mTestStartTimestamp = 0;
    
    private String mLastTestClass;
    
    public DefaultTestRunListener(BaseTask baseTask) {
        mTask  = baseTask;
    }

    public int getFailedTestCount() {
        return mFailedTestCount;
    }
    
    /**
     * Reports the start of a test run.
     *
     * @param runName the test run name
     * @param testCount total number of tests in test run
     */
    public void testRunStarted(String runName, int testCount) {
        mTask.info("Test run \"%s\" started: %d tests.", runName, testCount);
    }

    /**
     * Reports the start of an individual test case.
     *
     * @param test identifies the test
     */
    public void testStarted(TestIdentifier test) {
        mTotalTestCount++;
        if(!test.getClassName().equals(mLastTestClass)) {
            mLastTestClass = test.getClassName();
            System.out.println("  @" + test.getClassName()+": ");
        }
        
        System.out.print("    # " + test.getTestName());
        mTestFailed = false;
        mTestStartTimestamp = System.currentTimeMillis();
    }

    /**
     * Reports the failure of a individual test case.
     * <p/>
     * Will be called between testStarted and testEnded.
     *
     * @param status failure type
     * @param test identifies the test
     * @param trace stack trace of failure
     */
    public void testFailed(TestFailure status, TestIdentifier test, String trace) {
        mTestFailed = true;
        mFailedTestCount++;
        
        String tracePrefix = "      ";
        trace = tracePrefix + trace.trim();
        trace = trace.replaceAll("\\n", "\n" + tracePrefix + "  ");
        
        long elapsed = System.currentTimeMillis() - mTestStartTimestamp;
        System.out.println(String.format(": %s (elapsed %.2fs)", status, elapsed/1000.0));
        System.out.println(trace);
    }

    /**
     * Reports the execution end of an individual test case.
     * <p/>
     * If {@link #testFailed} was not invoked, this test passed.  Also returns any key/value
     * metrics which may have been emitted during the test case's execution.
     *
     * @param test identifies the test
     * @param testMetrics a {@link Map} of the metrics emitted
     */
    public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
        if(!mTestFailed) {
            long elapsed = System.currentTimeMillis() - mTestStartTimestamp;
            System.out.println(String.format(": SUCCESS (elapsed %.2fs)", elapsed / 1000.0));
        }
    }

    /**
     * Reports test run failed to complete due to a fatal error.
     *
     * @param errorMessage {@link String} describing reason for run failure.
     */
    public void testRunFailed(String errorMessage) {
        mTask.error("Test run failed: %s", errorMessage);
    }

    /**
     * Reports test run stopped before completion due to a user request.
     * <p/>
     * TODO: currently unused, consider removing
     *
     * @param elapsedTime device reported elapsed time, in milliseconds
     */
    public void testRunStopped(long elapsedTime) {
        mTask.error("Test run stopped: %.2f elapsed.", elapsedTime / 1000.0);
    }

    /**
     * Reports end of test run.
     *
     * @param elapsedTime device reported elapsed time, in milliseconds
     * @param runMetrics key-value pairs reported at the end of a test run
     */
    public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
        mTask.info("Test run ended: test run %d, test failed %d, elapsed %.2f .", 
                    mTotalTestCount, mFailedTestCount, elapsedTime / 1000.0);
    }

}
