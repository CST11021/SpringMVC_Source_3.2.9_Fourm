package com.whz.javabase.threadpool;

public interface ExecuteCallbackHandler<V> {

	public void process(V result);

}