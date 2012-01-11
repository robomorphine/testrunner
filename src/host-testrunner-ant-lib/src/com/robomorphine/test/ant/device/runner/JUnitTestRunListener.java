package com.robomorphine.test.ant.device.runner;

import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;

import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class JUnitTestRunListener implements ITestRunListener {
        
    @SuppressWarnings("unused")
    @XmlRootElement(name="testcase")
    public static class TestCase {
        
        @XmlAttribute(name="name") 
        private String mName;
        
        @XmlAttribute(name="classname")
        private String mClassname;
        
        @XmlElement(name="error")
        private String mError;
        
        @XmlElement(name="failure")
        private String mFailure;
        
        @XmlAttribute(name="time")
        private String mTime;
        
        private final TestIdentifier mId;
        private TestFailure mStatus;
        private String mTrace;
        private long mStartTime;
        private long mElapsedTime;
        
        public TestCase() {
            /* c-tor for JAXB only */
            throw new UnsupportedOperationException("");
        }
        
        public TestCase(TestIdentifier id) {
            mId = id;        
        }
        
        public void markStarted() {
            mStartTime = System.currentTimeMillis();
        }
        
        public void markFailed(TestFailure status, String trace) {
           mStatus = status;
           mTrace = trace;
        }
        
        public void markEnded() {
            mElapsedTime = System.currentTimeMillis() - mStartTime;
        }
        
        public long getElapsedTime() {
            return mElapsedTime;
        }
        
        public TestFailure getStatus() {
            return mStatus;
        }
        
        public void prepareForSerialization() {
            mName = mId.getTestName();
            mClassname = mId.getClassName();
            if(mStatus == TestFailure.ERROR) {
                mError = mTrace;
            } else {
                mFailure = mTrace;
            }
            mTime = String.format("%.3f", mElapsedTime / 1000.0);
        }
        
    }
    
    @SuppressWarnings("unused")
    @XmlRootElement(name="testsuite")
    public static class TestSuite {
        
        @XmlAttribute(name="name")
        private final String mName;
        
        @XmlAttribute(name="time")
        private String mTime;
        
        @XmlElement(name="testcase")
        private final List<TestCase> mTests = new LinkedList<TestCase>();
        private final Map<String, TestCase> mTestMap = new HashMap<String, TestCase>();
        
        private int mFailureCount = 0;
        private int mErrorCount = 0;
        
        public TestSuite() {
            throw new UnsupportedOperationException("");
        }
        
        public TestSuite(String name) {
            mName = name;
        }
        
        public String getName() {
            return mName;
        }
        
        public TestCase getTest(String testName) {
            TestCase testCase = mTestMap.get(testName);
            if(testCase == null) {
                testCase = new TestCase(new TestIdentifier(mName, testName));
                mTests.add(testCase);
                mTestMap.put(testName, testCase);
            }
            return testCase;
        }
        
        public void prepareForSerialization() {
            long totalTime = 0;
            mErrorCount = 0;
            mFailureCount = 0;
            for(TestCase testCase : mTests) {
                testCase.prepareForSerialization();
                totalTime += testCase.getElapsedTime();
                
                TestFailure status = testCase.getStatus();
                if(status != null) {
                    if(status == TestFailure.ERROR) {
                        mErrorCount++;
                    } else {
                        mFailureCount++;
                    }
                }
            }
            mTime = String.format("%.3f", totalTime / 1000.0);
        }
        
        public int getTotalCount() {
            return mTests.size();
        }
        
        public int getFailureCount() {
            return mFailureCount;
        }
        
        public int getErrorCount() {
            return mErrorCount;
        }
    }
    
    @SuppressWarnings("unused")
    @XmlRootElement(name="testrun")
    public static class TestRun {
        
        @XmlAttribute(name="name")
        private final String mName;
        @XmlAttribute(name="tests")
        private final int mTotalCount;
        @XmlAttribute(name="started")
        private int mStartedCount;
        @XmlAttribute(name="failures")
        private int mFailureCount;
        @XmlAttribute(name="errors")
        private int mErrorCount;
        @XmlAttribute(name="ignored")
        private int mIgnoredCount;
        
        @XmlElement(name="testsuite")
        private final List<TestSuite> mSuites = new LinkedList<TestSuite>();
        private final Map<String, TestSuite> mSuiteMap = new HashMap<String, TestSuite>();
        
        public TestRun() {
            throw new UnsupportedOperationException("");
        }
        
        public TestRun(String name, int testCount) {
            mName = name;
            mTotalCount = testCount;
        }
        
        public TestCase getTest(TestIdentifier id) {
            String classname = id.getClassName();
            TestSuite suite = mSuiteMap.get(classname);
            if(suite == null) {
                suite = new TestSuite(classname);
                mSuiteMap.put(classname, suite);
                mSuites.add(suite);
            }
            return suite.getTest(id.getTestName());
        }
        
        public void prepareForSerialization() {
            int started = 0;
            int failures = 0;
            int errors = 0;
            
            for(TestSuite suite : mSuites) {
                suite.prepareForSerialization();
                started += suite.getTotalCount();
                failures += suite.getFailureCount();
                errors += suite.getErrorCount();
            }
            mStartedCount = started;
            mFailureCount = failures;
            mErrorCount = errors;
            mIgnoredCount = mTotalCount - started;
        }
        
        private JAXBContext newJAXBContext() throws JAXBException {
            return JAXBContext.newInstance(TestCase.class, TestSuite.class, TestRun.class);
        }
    
        
        public void saveRun(File reportFile) throws IOException, JAXBException {
            JAXBContext context = newJAXBContext();
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            if(reportFile.isDirectory()) {
                reportFile = new File(reportFile, mName + ".xml");
            }
            
            FileOutputStream fout = new FileOutputStream(reportFile);
            try {
                marshaller.marshal(this, fout);
            } finally {
                fout.close();
            }
        }
        
        public void saveSuites(File reportDir) throws IOException, JAXBException {
            JAXBContext context = newJAXBContext();
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            for(TestSuite suite : mSuites) {
                String filename = mName + "-" + suite.getName() + ".xml";
                File reportFile = new File(reportDir, filename);
                FileOutputStream fout = new FileOutputStream(reportFile);
                
                try {
                    marshaller.marshal(suite, fout);
                } finally {
                    fout.close();
                }
            }
        }
    }
    
    private TestRun mTestRun;
    private final File mOutputDir;
    private final boolean mMultiple;
    
    public JUnitTestRunListener(File outputDir, boolean multiple) {
        mOutputDir = outputDir;
        mMultiple = multiple;
    }
    
    public void save() {
        mOutputDir.mkdirs();
        try {
            mTestRun.prepareForSerialization();
            if(mMultiple) {
                mTestRun.saveSuites(mOutputDir);
            } else {
                mTestRun.saveRun(mOutputDir);
            }
        } catch(Exception ex) {
            throw new BuildException("Failed to save JUnit test report(s).", ex);
        }
    }
    
    public void testRunStarted(String runName, int testCount) {
        mTestRun = new TestRun(runName, testCount);
    }
    
    public void testStarted(TestIdentifier test) {
        TestCase testCase = mTestRun.getTest(test);
        testCase.markStarted();
    }
    
    public void testFailed(TestFailure status, TestIdentifier test, String trace) {
        TestCase testCase = mTestRun.getTest(test);
        testCase.markFailed(status, trace);
    }
    
    public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
        TestCase testCase = mTestRun.getTest(test);
        testCase.markEnded();
    }

    public void testRunFailed(String errorMessage) {
        /* create "marker" test and fail it so test report shows that run failed */
        TestCase test = mTestRun.getTest(new TestIdentifier("TestRunFailed", "markerTest"));
        test.markStarted();
        test.markFailed(TestFailure.ERROR, errorMessage);
        save();
    }

    public void testRunStopped(long elapsedTime) {
        /* create "marker" test and fail it so test report shows that run failed */
        TestCase test = mTestRun.getTest(new TestIdentifier("TestRunStopped", "markerTest"));
        test.markStarted();
        test.markFailed(TestFailure.ERROR, "Test run stopped by user.");
        save();
    }
    
    public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
        save();
    }
}
