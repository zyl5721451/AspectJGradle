package com.sohu.aspectj;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by allenzhang on 2019/3/22.
 */
@Aspect
public class AllAspect {
    @Pointcut("execution(void android.support.v4.app.Fragment.onResume()) && target(fragment)")
    public void onResume(Fragment fragment) {}

    @Pointcut("execution(void testParams(..)) && within(com.sohu.aspectj.MainFragment) && target(fragment) && args(visible)")
    public void testParams(MainFragment fragment, boolean visible) {}

    @Pointcut("execution(void android.view.View.OnClickListener.onClick(..))  && args(view)")
    public void onClick(View view) {}

    //com.sohu.aspectj包以及子包下，接口中的方法
    @Pointcut("execution(void com.sohu.aspectj..CustomerClass.onClick(..))  && args(view)")
    public void onMainActivityClick(View view) {}




//    @Before("onResume(fragment)")
//    public void beforeOnResume(Fragment fragment) {
//        Log.d("chao", fragment.getClass().getSimpleName() + " before");
//    }
//    @After("onResume(fragment)")
//    public void afterOnResume(Fragment fragment,JoinPoint joinPoint) {
//        Log.d("chao", fragment.getClass().getSimpleName() + " after");
//    }
//    @Around("onResume(fragment)")
//    public Object aroundOnResume(Fragment fragment,ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
//        Log.d("chao", fragment.getClass().getSimpleName() + " around");
//        return proceedingJoinPoint.proceed();
//    }

//    @Before("execution(* android.app.Activity+.on**(..))")//返回值 类 方法，+代表子类，(..)代表任何参数
//    public void onActivityMethodBefore(JoinPoint joinPoint) {
//        Log.d("chao", joinPoint.getSignature().getDeclaringTypeName() + " ActivityOn..");
//    }

//    @Before("execution(* android.support.v4.app.Fragment+.on**(..))")//返回值 类 方法，+代表子类，(..)代表任何参数
//    public void onFragmentOnBefore(JoinPoint joinPoint) {
//        Log.d("chao", joinPoint.getSignature().getDeclaringTypeName() +":"+ joinPoint.getSignature().getName());
//    }

//    @Before("testParams(fragment,visible)")
//    public void beforeseTtestParams(Fragment fragment,boolean visible) {
//        Log.d("chao","beforesetestParams:"+visible);
//    }

//    @Before("onClick(view)")
//    public void beforeOnClick(View view,JoinPoint joinPoint) {
//        Log.d("chao","beforeOnClick:"+joinPoint.getSignature().getDeclaringType()+":"+joinPoint.getSignature().getDeclaringTypeName());
//    }
//    @Before("onMainActivityClick(view)")
//    public void beforeMainActiviytOnClick(View view,JoinPoint joinPoint) {
//        Log.d("chao","beforeMainActiviytOnClick:"+joinPoint.getSignature().getDeclaringType()+":"+joinPoint.getSignature().getDeclaringTypeName());
//    }

}
