
package com.robomorphine.test.testrunner;

import com.robomorphine.test.annotation.DisabledTest;
import com.robomorphine.test.annotation.EnabledTest;
import com.robomorphine.test.annotation.LongTest;
import com.robomorphine.test.annotation.ManualTest;
import com.robomorphine.test.annotation.NonUiTest;
import com.robomorphine.test.annotation.PerformanceTest;
import com.robomorphine.test.annotation.ShortTest;
import com.robomorphine.test.annotation.StabilityTest;
import com.robomorphine.test.annotation.UiTest;

import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestCase;

public class TestFilterTestRunner extends TestCase {
    
    @android.test.suitebuilder.annotation.SmallTest
    public void testAsAndroidSmall() { //NOPMD
    }

    @SmallTest
    public void testAsSmall() { //NOPMD

    }

    @ShortTest
    public void testAsShort() { //NOPMD

    }

    @MediumTest
    public void testAsMedium() { //NOPMD

    }
    
    @android.test.suitebuilder.annotation.MediumTest
    public void testAsAndroidMedium() { //NOPMD

    }

    @LargeTest
    public void testAsLarge() { //NOPMD

    }
    
    @android.test.suitebuilder.annotation.LargeTest
    public void testAsAndroidLarge() { //NOPMD

    }
    
    @LongTest
    public void testAsLong() { //NOPMD
        
    }

    @ManualTest
    public void testAsManual() { //NOPMD

    }

    @PerformanceTest
    public void testAsPerfomance() { //NOPMD

    }
    
    @StabilityTest
    public void testAsStability() { //NOPMD

    }
    
    @UiTest
    public void testAsUi() { //NOPMD

    }
    
    @NonUiTest
    public void testAsNonUi() { //NOPMD

    }
    
    @UiTest
    @PerformanceTest
    public void testAsUiPerformance() { //NOPMD

    }
    
    @NonUiTest
    @PerformanceTest
    public void testAsNonUiPerformance() { //NOPMD

    }
    
    @EnabledTest
    public void testEnabled() { //NOPMD

    }
    
    @DisabledTest("test")
    public void testDisabled() { //NOPMD

    }
}
