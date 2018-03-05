package com.whz.javabase.threadpool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolService {

	private static int capacity = 20;
	// 表示线程池维护线程的最少数量
	private static int minimumPoolSize = 100;
	// 表示线程池维护线程的最大数量
	private static int maximumPoolSize = 100;
	private static String taskName = "Worker";

	//	minimumPoolSize： 线程池维护线程的最少数量
	//	maximumPoolSize：线程池维护线程的最大数量
	//	keepAliveTime： 线程池维护线程所允许的空闲时间
	//	unit： 线程池维护线程所允许的空闲时间的单位
	//	workQueue： 线程池所使用的缓冲队列
	//	handler： 线程池对拒绝任务的处理策略
	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(minimumPoolSize, maximumPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory());

	private static ThreadPoolService instance = new ThreadPoolService(capacity, minimumPoolSize, maximumPoolSize, taskName, executor);

	private ThreadPoolService(int capacity, int minimumPoolSize, int maximumPoolSize, String taskName, ThreadPoolExecutor executor) {
		this.capacity = capacity;
		this.minimumPoolSize = minimumPoolSize;
		this.maximumPoolSize = maximumPoolSize;
		this.taskName = taskName;
		this.executor = executor;
	}

	public static ThreadPoolService getInstance(){
		return instance;
	}

	//提供执行任务接口，代理方法
	public void execute(Runnable command) throws Exception {
		if(isFull()) {
			throw new Exception("线程池已满");
		}
		executor.execute(command);
    }

	//提供执行任务接口，代理方法,包级别，不提供外部使用
	void forceExecute(Runnable command) {
		executor.execute(command);
    }

	//提供执行任务接口，代理方法
	public <T> Future<T> submit(Callable<T> task) throws Exception {
		if(isFull()) {
			throw new Exception("线程池已满");
		}
		return executor.submit(task);
	}

	//获取当前正在工作的任务数
	public int getActiveWorkerCount() {
		return executor.getActiveCount();
	}

	//获取当前正在使用的线程池队列大小
	public int getQueueSize(){
		return executor.getQueue().size();
	}

	//判断线程池是否已满
	public boolean isFull(){
		synchronized (executor) {
			return executor.getPoolSize() >= executor.getMaximumPoolSize() && executor.getQueue().size() >= capacity;
		}
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public static void setMinimumPoolSize(int minimumPoolSize) {
		ThreadPoolService.minimumPoolSize = minimumPoolSize;
	}

	public static void setMaximumPoolSize(int maximumPoolSize) {
		ThreadPoolService.maximumPoolSize = maximumPoolSize;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	private static class DefaultThreadFactory implements ThreadFactory {
        private ThreadGroup group;
        private AtomicInteger threadNumber = new AtomicInteger(1);

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                                 Thread.currentThread().getThreadGroup();
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, taskName + "-" + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}

