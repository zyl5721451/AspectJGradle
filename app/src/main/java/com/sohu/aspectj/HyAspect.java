package com.sohu.aspectj;

import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.concurrent.TimeUnit;

/**
 * Created by allenzhang on 2019/3/22.
 */
@Aspect
public class HyAspect {
    @Pointcut("execution(@com.sohu.aspectj.AspectLog * *(..))&&@annotation(hyaspect)")
    public void method(AspectLog hyaspect) {
    } // @AspectLog 修饰的方法的执行
    //另外一种写法
    @Pointcut("execution(@com.sohu.aspectj.AspectLog * *(..))")
    public void method2() {
    } // @AspectLog 修饰的方法的执行

    @Pointcut("execution(@com.sohu.aspectj.AspectLog *.new(..))&&@annotation(hyaspect)")
    public void constructor(AspectLog hyaspect) {

    } // @AspectLog 修饰的构造函数的执行

    @Pointcut("execution(@com.sohu.aspectj.AspectLog *.new(..))")
    public void constructor2() {

    } // @AspectLog 修饰的构造函数的执行

    @Around("method(hyaspect) || constructor(hyaspect)")
    public Object logAndExecute(ProceedingJoinPoint joinPoint,AspectLog hyaspect) throws Throwable {
        String[] checkString = hyaspect.checkString();
        //-----AspectLog:needCheck:false checkCode:3 checkString:a
        Log.d("chao", "-----AspectLog:needCheck:" + hyaspect.needCheck()+" checkCode:"+hyaspect.checkCode()+" checkString:"+checkString[0]);
        //-----className:class com.sohu.aspectj.MainActivity
        Log.d("chao", "-----className:" + joinPoint.getSignature().getDeclaringType());
       // -----Methodname:execute
        Log.d("chao", "-----Methodnaconstructor2me:" + joinPoint.getSignature().getName());
        //-----ParamsType:class java.lang.String
        Log.d("chao", "-----ParamsType:" + ((CodeSignature)(joinPoint.getSignature())).getParameterTypes()[0]);
       //-----ParamsValue:myExecute
        Log.d("chao", "-----ParamsValue:" + joinPoint.getArgs()[0]);
        boolean hasReturnType = joinPoint.getSignature() instanceof MethodSignature
                && ((MethodSignature) joinPoint.getSignature()).getReturnType() != void.class;
        //-----returnType:false
        Log.d("chao", "-----returnType:" + hasReturnType);
        long startNanos = System.nanoTime();
        Object result = joinPoint.proceed(); // 调用原来的方法
        long stopNanos = System.nanoTime();
        long lengthMillis = TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);
        //-----executeTime:500
        Log.d("chao", "-----executeTime:" + lengthMillis);
        return result;
    }

    @Around("method2() || constructor2()")
    public Object logAndExecute2(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
        AspectLog hyaspect = methodSignature.getMethod().getAnnotation(AspectLog.class);
        String[] checkString = hyaspect.checkString();
        Log.d("chao", "-----AspectLog2:needCheck:" + hyaspect.needCheck()+" checkCode:"+hyaspect.checkCode()+" checkString:"+checkString[0]);
        Object result = joinPoint.proceed(); // 调用原来的方法
        return result;
    }
}
