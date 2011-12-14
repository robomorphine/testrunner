package com.robomorphine.test;

import com.robomorphine.test.log.ILog;
import com.robomorphine.test.log.StdLog;

import java.io.File;

import junit.framework.TestCase;

public class TestRunnerTest extends TestCase {

    public void testTestRunner() throws Exception {
        File path = new File("r:\\repository\\dev\\bin\\android-sdk");
        ILog logger = new StdLog();        
        
        TestManager testManager = new TestManager(path, logger);
        testManager.connectAdb();
        
        TestRunner runner = new TestRunner(testManager);
        File tester = new File("r:\\repository\\dev\\src\\projects\\github\\robomorphine-testrunner\\bin\\debug\\tester.app.apk");
        File tested = new File("r:\\repository\\dev\\src\\projects\\github\\robomorphine-testrunner\\bin\\debug\\tested.app.apk");
        runner.run("emulator-5554", tester, tested);
    }
}
