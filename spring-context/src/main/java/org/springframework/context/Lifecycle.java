
package org.springframework.context;

// Spring 提供Lifecycle接口，它包含start/stop 方法，实现此接口后Spring会保证在启动的时候调用期start方法开始生命周期，并在Spring关闭的时候调用stop方法来结束生命周期，
// 通常用来配置后台程序，在启动后一直运行（如对MQ进行轮询等）。
public interface Lifecycle {

	// Start this component.
	void start();

	// Stop this component
	void stop();

	// 判断该组件是否启动，返回true的时候，容器销毁时会调用stop()方法
	boolean isRunning();

}
