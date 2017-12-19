package com.whz.aop.aspectJ;


import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;


@Aspect
public class AspectBean2 {

   @DeclareParents(value="com.whz.aop.aspectJ.NaiveWaiter", defaultImpl=SmartSeller.class)
   public  Seller seller;

}
