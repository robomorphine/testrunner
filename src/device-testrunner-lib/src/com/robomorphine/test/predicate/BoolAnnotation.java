package com.robomorphine.test.predicate;

import android.test.suitebuilder.TestMethod;

import java.lang.annotation.Annotation;

public class BoolAnnotation implements Predicate<TestMethod>{ // NOPMD 
    
    private final Class<? extends Annotation> mPositive;
    private final Class<? extends Annotation> mNegative;
    private final HasMethodAnnotation mMethodHasPositiveAnnotation;
    private final HasMethodAnnotation mMethodHasNegativeAnnotation;
    private final HasClassAnnotation mClassHasPositiveAnnotation;
    private final HasClassAnnotation mClassHasNegativeAnnotation;
    
    public BoolAnnotation(Class<? extends Annotation> positive, 
                                   Class<? extends Annotation> negative) {
        mPositive = positive;
        mNegative = negative;
        mMethodHasPositiveAnnotation = new HasMethodAnnotation(mPositive);
        mMethodHasNegativeAnnotation = new HasMethodAnnotation(mNegative);
        mClassHasPositiveAnnotation = new HasClassAnnotation(mPositive);
        mClassHasNegativeAnnotation = new HasClassAnnotation(mNegative);
    }
    
    public Class<? extends Annotation> getPositive() {
        return mPositive;
    }
    
    public Class<? extends Annotation> getNegative() {
        return mNegative;
    }
    
    protected boolean isPositive(Class<? extends Annotation> annotation) {
        return mPositive.equals(annotation);
    }
    
    protected boolean isNegative(Class<? extends Annotation> annotation) {
        return mNegative.equals(annotation);
    }
    
    protected boolean negativeHasPriority() {
        return true;
    }
    
    protected Class<? extends Annotation> calculateEffectiveAnnotation(TestMethod method) { //NOPMD       
        if(negativeHasPriority()) {
            if(mMethodHasNegativeAnnotation.apply(method)) return mNegative; //NOPMD            
            if(mMethodHasPositiveAnnotation.apply(method)) return mPositive; //NOPMD
            if(mClassHasNegativeAnnotation.apply(method)) return mNegative;  //NOPMD
            if(mClassHasPositiveAnnotation.apply(method)) return mPositive;  //NOPMD
        } else {            
            if(mMethodHasPositiveAnnotation.apply(method)) return mPositive; //NOPMD           
            if(mMethodHasNegativeAnnotation.apply(method)) return mNegative; //NOPMD            
            if(mClassHasPositiveAnnotation.apply(method)) return mPositive;  //NOPMD          
            if(mClassHasNegativeAnnotation.apply(method)) return mNegative;  //NOPMD
        }
        return null;
    }
    
    protected Class<? extends Annotation> calculateDefaultAnnotation(TestMethod method) {
        if(negativeHasPriority()) {
            return mNegative;
        } else {
            return mPositive;
        }        
    }
    
    @Override
    public boolean apply(TestMethod t) {
        Class<? extends Annotation> annotation = calculateEffectiveAnnotation(t);
        if(annotation == null) {
            annotation = calculateDefaultAnnotation(t);
        }        
        return annotation.equals(mPositive);
    };
    
    @Override
    public String toString() {
        return String.format("[bool-annotation %s/%s]",
                              mPositive.getSimpleName(), 
                              mNegative.getSimpleName());
    }
}
