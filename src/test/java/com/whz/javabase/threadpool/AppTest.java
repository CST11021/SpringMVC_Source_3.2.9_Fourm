package com.whz.javabase.threadpool;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AppTest {

	@Test
	public void test(){
		List queryResult = new ArrayList<>();

		MultiThreadExecutor executor = new MultiThreadExecutor<QueryResult>();
		//先将所有的任务都提交给线程池，具体的执行顺序由线程池解决，这样多个任务便可以异步执行
		for(int i=1;i<10;i++){
			executor.submit(new QueryRemoteTask("res_"+i));
		}

		//获取结果时executor.getAllResult()方法将遍历completionQueue队列，将里面的结果一个个get出来，
		// 注意调用future.get()方法时，主线程会进入等待状态，直到获取所有的查询结果后才停止等待。
//		queryResult = executor.getAllResult();
		executor.getAllResult(new ExecuteCallbackHandler<QueryResult>() {
			@Override
			public void process(QueryResult result) {
				queryResult.add(result);
			}
		});

		//此时已经将所有查询结果放入queryResult中，继续执行以下操作。。。
		System.out.println(queryResult);
	}
}