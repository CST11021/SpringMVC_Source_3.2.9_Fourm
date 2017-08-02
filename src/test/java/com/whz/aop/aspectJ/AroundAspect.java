package com.whz.aop.aspectJ;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Created by wb-whz291815 on 2017/7/31.
 */
public class AroundAspect {

    public Object process(ProceedingJoinPoint point) throws Throwable {

        try {
            String clazzName = point.getTarget().getClass().getSimpleName();
            String methodName = point.getSignature().getName();
            System.out.println("before invoked [clazzName:" + clazzName + " methodName:" + methodName + "]");
            point.proceed();
            System.out.println("after invoked [clazzName:" + clazzName + " methodName:" + methodName + "]");
            return null;
        } catch (Throwable throwable) {
            return throwable;
        }
    }

}