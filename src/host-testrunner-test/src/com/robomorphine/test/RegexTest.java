package com.robomorphine.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class RegexTest extends TestCase {

    public void testRegex() {
        Pattern pattern = Pattern.compile("(?i)(\\d\\d+)(px|dp|)[x|\\\\|/|\\*](\\d\\d+)(px|dp|)");
        String [] matches = new String []  {
                "100x100", "100/100", "100*100", "100|100", "100px*200dp", "100*200px", "100Dp/300pX"
        };
        
        for(String v : matches) {
            Matcher matcher = pattern.matcher(v);
            assertTrue(v, matcher.matches());
        }
    }
}
