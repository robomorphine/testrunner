package com.robomorphine.tester;

import com.robomorphine.tested.MainActivity;

import android.test.ActivityInstrumentationTestCase2;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity>{
    
    public MainActivityTest() {
        super(null, MainActivity.class);
    }
    
    public void testShowActivity() {
        getActivity();
        getInstrumentation().waitForIdleSync();
    }
}
