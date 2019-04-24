package com.sohu.aspectj;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by allenzhang on 2019/3/25.
 */
@Aspect
public class SampleAspect {
    @Pointcut("execution(void android.support.v4.app.FragmentActivity+.onCreate(..))")
    public void activityOnCreate() {

    }

    @Pointcut("execution(@com.sohu.aspectj.AspectLog public static final boolean com.sohu.aspectj.MainActivity.getIsAnnotation(java.lang.String,boolean)throws java.lang.Exception)")
    public void activityIsFirst() {

    }

//    @Pointcut("execution(@com.sohu.aspectj.AspectLog public static final boolean com.sohu.aspectj.MainActivity.new(java.lang.String,boolean)throws java.lang.Exception)")
//    public void activityInit() {
//
//    }

    //Fragment及其子类onCreate方法的调用
    @Pointcut("execution(public * android.support.v4.app.Fragment+.onCreate(..))")
    public void fragmentOnCreate() {

    }


    @Pointcut("execution(public * testParams(..)) && target(fragment) && args(args)")
    public void fragmentTestParams(boolean args,Fragment fragment) {

    }

    @Pointcut("execution(public * testParams(..)) && target(android.support.v4.app.Fragment+) && args(args)")
    public void fragmentTesttargetParams(boolean args,Fragment fragment) {

    }
    @Pointcut("execution(public * testParams(..)) && within(android.support.v4.app.Fragment+) && args(args)")
    public void fragmentWithTestParams(boolean args,Fragment fragment) {

    }

    @Pointcut("execution(public * testParams(..))&& args(args)")
    public void fragmentWithTestMoreParams(boolean args,Fragment fragment) {

    }

    @Pointcut("execution(@com.sohu.aspectj.AspectLog * com.sohu.aspectj..*(..))")
    public void fragmentaspectLog(Fragment fragment) {

    }

    @Pointcut("handler(java.lang.Exception)&&within(com.sohu.aspectj.*)")
    public void fragmentException() {

    }





//    @Before("activityOnCreate()")
//    public void onCreateBefore(JoinPoint joinPoint) {
//        Log.d("chao", "onCreateAdvice" + joinPoint.getSignature().getDeclaringType() + ":" + joinPoint.getSignature().getDeclaringTypeName());
//    }
//    @After("activityOnCreate()")
//    public void onCreateAfter(JoinPoint joinPoint) {
//        Log.d("chao", "onCreateAdvice" + joinPoint.getSignature().getDeclaringType() + ":" + joinPoint.getSignature().getDeclaringTypeName());
//    }
//    @Around("activityOnCreate()")
//    public Object onCreateAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
//        Log.d("chao", "onCreateAdvice" + proceedingJoinPoint.getSignature().getDeclaringType() + ":" + proceedingJoinPoint.getSignature().getDeclaringTypeName());
//        return proceedingJoinPoint.proceed();
//    }

//    @Before("activityIsFirst()")
//    public void onCreateFirstBefore(JoinPoint joinPoint) {
//        Log.d("chao", "onCreateAdvice" + joinPoint.getSignature().getDeclaringType() + ":" + joinPoint.getSignature().getDeclaringTypeName());
//    }
//
//    @Before("activityInit()")
//    public void activityInitBefore(JoinPoint joinPoint) {
//        Log.d("chao", "activityInitBefore" + joinPoint.getSignature().getDeclaringType() + ":" + joinPoint.getSignature().getDeclaringTypeName());
//    }
//
//    @Before("fragmentOnCreate()")
//    public void fragmentOnCreate(JoinPoint joinPoint) {
//        Log.d("chao", "activityInitBefore" + joinPoint.getSignature().getDeclaringType() + ":" + joinPoint.getSignature().getDeclaringTypeName());
//    }
//
//    @Before("fragmentTestParams(args,fragment)")
//    public void fragmentTestParams(JoinPoint joinPoint,boolean args,Fragment fragment) {
//        Log.d("chao", "fragmentTestParams" + joinPoint.getSignature().getDeclaringType() + ":" + joinPoint.getSignature().getDeclaringTypeName()+":"+args);
//
//    }
//
//    @Before("fragmentWithTestParams(args,fragment)")
//    public void fragmentWithinTestParams(JoinPoint joinPoint,boolean args,Fragment fragment) {
//        Log.d("chao", "fragmentWithTestParams" + joinPoint.getSignature().getDeclaringType() + ":" + joinPoint.getSignature().getDeclaringTypeName()+":"+args);
//
//    }
//
//    @Before("fragmentWithTestMoreParams(args,fragment)")
//    public void fragmentWithTestMoreParams(JoinPoint joinPoint,boolean args,Fragment fragment) {
//        Log.d("chao", "fragmentWithTestMoreParams" + joinPoint.getSignature().getDeclaringType() + ":" + joinPoint.getSignature().getDeclaringTypeName()+":"+args);
//
//    }
//
//
//    @Before("fragmentaspectLog(fragment)")
//    public void fragmentaspectLog(JoinPoint joinPoint,Fragment fragment) {
//        Log.d("chao", "fragmentaspectLog" + joinPoint.getSignature().getDeclaringType() + ":" + joinPoint.getSignature().getDeclaringTypeName()+":");
//
//    }


    @Before("fragmentException()")
    public void fragmentException(JoinPoint joinPoint) {
        Log.d("chao", "fragmentException" + joinPoint.getSignature().getDeclaringType() + ":" + joinPoint.getSignature().getDeclaringTypeName()+":");

    }


}
