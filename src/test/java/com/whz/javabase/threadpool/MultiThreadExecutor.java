package com.whz.javabase.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MultiThreadExecutor<V> {
	// 记录线程池中任务数
	private int count;
	// 线程池服务类
	private ThreadPoolService threadPoolService;
	// 用于存放查询结果
	private final BlockingQueue<Future<V>> completionQueue;

	
	public MultiThreadExecutor(){
		this.count = 0;
		this.threadPoolService = ThreadPoolService.getInstance();
		this.completionQueue = new LinkedBlockingQueue<>();
	}
	
	public void submit(Callable<V> task){
		if (task != null) {
			count ++;
			//new QueueingFutureTask(task)会把任务加入到completionQueue
			threadPoolService.forceExecute(new QueueingFutureTask(task));
		}
	}

	//将所有的查询结果以List的形式返回
	public List<V> getAllResult(){
		List<V> allResult = new ArrayList<V>();
		
		V result = null;
		for (int i = 0; i < count; i++) {
			try {
				result = completionQueue.take().get();
			} catch (InterruptedException ex) {
				System.out.println("执行任务过程中断");
				continue;
			} catch (ExecutionException ex) {
				System.out.println("执行任务过程过程失败");
				continue;
			}
			allResult.add(result);
		}
		
		return allResult;
	}
	//使用回调的方式返回查询结果
	public void getAllResult(ExecuteCallbackHandler<V> callback){
		V result;
		for (int i = 0; i < count; i++) {
			try {
				result = completionQueue.take().get();
			} catch (InterruptedException ex) {
				System.out.println("执行任务过程中断");
				continue;
			} catch (ExecutionException ex) {
				System.out.println("执行任务过程过程失败");
				continue;
			}

			callback.process(result);
		}
	}


	private class QueueingFutureTask extends FutureTask<V> {
		QueueingFutureTask(Callable<V> task) {
            super(task);
        }

		//开始执行任务的时候调用该方法
		@Override
        protected void done() {
			//按任务的执行顺序将任务放到队列中
			completionQueue.add(this);
		}
    }
	
}

