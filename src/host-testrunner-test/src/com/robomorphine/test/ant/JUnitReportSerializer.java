package com.robomorphine.test.ant;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.android.ddmlib.testrunner.ITestRunListener.TestFailure;
import com.robomorphine.test.ant.device.runner.JUnitTestRunListener.TestCase;
import com.robomorphine.test.ant.device.runner.JUnitTestRunListener.TestRun;
import com.robomorphine.test.ant.device.runner.JUnitTestRunListener.TestSuite;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;



public class JUnitReportSerializer extends junit.framework.TestCase {
    
    public void testSerializeTestCase() throws Exception {
        TestCase testCase = new TestCase(new TestIdentifier("test", "class"));
        testCase.markStarted();
        Thread.sleep(134);
        testCase.markEnded();
        testCase.prepareForSerialization();
        
        JAXBContext context = JAXBContext.newInstance(TestCase.class, TestSuite.class, TestRun.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(testCase, System.out);
        System.out.println();
    }
    
    public void testSerializeTestCase_failure() throws Exception {
        TestCase testCase = new TestCase(new TestIdentifier("test", "class"));
        testCase.markStarted();
        Thread.sleep(134);
        testCase.markFailed(TestFailure.FAILURE, "failure traces");
        testCase.markEnded();
        testCase.prepareForSerialization();
        
        JAXBContext context = JAXBContext.newInstance(TestCase.class, TestSuite.class, TestRun.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(testCase, System.out);
        System.out.println();
    }
    
    public void testSerializeTestCase_error() throws Exception {
        TestCase testCase = new TestCase(new TestIdentifier("test", "class"));
        testCase.markStarted();
        Thread.sleep(134);
        testCase.markFailed(TestFailure.ERROR, "error traces");
        testCase.markEnded();
        testCase.prepareForSerialization();
        
        JAXBContext context = JAXBContext.newInstance(TestCase.class, TestSuite.class, TestRun.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(testCase, System.out);
        System.out.println();
    }
    
    public void testSerializeTestSuite() throws Exception {
        TestSuite suite = new TestSuite("suite");
        for(int i = 0; i < 3; i++) {
            TestCase testCase = suite.getTest("test" + i);
            testCase.markStarted();
            Thread.sleep(13);
            testCase.markEnded();
        }
        
        TestCase testCase = suite.getTest("failed-test");
        testCase.markStarted();
        Thread.sleep(13);
        testCase.markFailed(TestFailure.FAILURE, "failute");
        testCase.markEnded();
        
        testCase = suite.getTest("error-test");
        testCase.markStarted();
        Thread.sleep(13);
        testCase.markFailed(TestFailure.ERROR, "failute");
        testCase.markEnded();
        
        suite.prepareForSerialization();
        JAXBContext context = JAXBContext.newInstance(TestCase.class, TestSuite.class, TestRun.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(suite, System.out);
        System.out.println();
    }
    
    public void testSerializeTestRun() throws Exception {
        TestRun testRun = new TestRun("run", 100);
        
        for(int s = 0; s < 3; s++) {
            
            for(int i = 0; i < 3; i++) {
                TestCase testCase = testRun.getTest(new TestIdentifier("suite"+s, "test" + i));
                testCase.markStarted();
                Thread.sleep(13);
                testCase.markEnded();
            }
            
            TestCase testCase = testRun.getTest(new TestIdentifier("suite"+s, "test-failed"));
            testCase.markStarted();
            Thread.sleep(13);
            testCase.markFailed(TestFailure.FAILURE, "failute");
            testCase.markEnded();
            
            testCase = testRun.getTest(new TestIdentifier("suite"+s, "test-error"));
            testCase.markStarted();
            Thread.sleep(13);
            testCase.markFailed(TestFailure.ERROR, "failute");
            testCase.markEnded();
        }
        
        testRun.prepareForSerialization();
        JAXBContext context = JAXBContext.newInstance(TestCase.class, TestSuite.class, TestRun.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(testRun, System.out);
        System.out.println();
    }
    
}
