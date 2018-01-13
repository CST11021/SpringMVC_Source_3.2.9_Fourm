package com.whz.aop.advice.introinterceptor;

// 该类用于被织入目标类
public class PerformanceMonitor{

	private static ThreadLocal<MethodPerformance> performanceRecord = new ThreadLocal<MethodPerformance>();

	public static void begin(String method) {
        System.out.println("begin monitor...");
		MethodPerformance mp = new MethodPerformance(method);
		performanceRecord.set(mp);
	}

	public static void end(){
		System.out.println("end monitor...");
		MethodPerformance mp = performanceRecord.get();
		mp.printPerformance();
	}

}