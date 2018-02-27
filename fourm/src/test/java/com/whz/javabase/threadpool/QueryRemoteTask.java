package com.whz.javabase.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

// 用于封装提交给线程池的任务，我们实现Callable接口，在call()方法中调用其他服务，然后将返回结果封装到QueryResult对象中
public class QueryRemoteTask implements Callable<QueryResult> {

    private String resId;

    public QueryRemoteTask(String resId) {
//        System.out.println("初始化QueryRemoteExecutor："+queryExp);
        this.resId = resId;
    }

    @Override
    public QueryResult call() throws Exception {
        System.out.println("执行任务，查询"+resId+"资源");

        List<Map<String,String>> result = new ArrayList();//queryResult用来模拟服务返回的数据对象
        QueryResult queryResult = new QueryResult(resId,result);//这里可以调用一些服务什么，让后我们再对返回的结果进行封装，比如将查询资源与查询结果做一次封装。
        return queryResult;
    }

}
