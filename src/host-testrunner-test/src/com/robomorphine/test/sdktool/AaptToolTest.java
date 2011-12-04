package com.robomorphine.test.sdktool;

import com.robomorphine.test.sdktool.SdkTool.Result;

import junit.framework.TestCase;

public class AaptToolTest extends TestCase {
    public void testExtractPackageName() {
        //
        
        String pkgName = "com.robomorphine.tester.app";
        String line = "package: name='" + pkgName + "' versionCode='1' versionName='dev'";
        Result result = new Result(1, line, "");
        
        String parsedPkgName = AaptTool.extractPackageName(result);
        assertEquals(pkgName, parsedPkgName);
    }
}
