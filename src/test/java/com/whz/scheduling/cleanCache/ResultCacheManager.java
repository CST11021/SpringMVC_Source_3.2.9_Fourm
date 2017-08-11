
package com.whz.scheduling.cleanCache;

//import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Service("resultCacheManager")
public class ResultCacheManager {
	private static final Logger logger = LoggerFactory.getLogger(ResultCacheManager.class);
	private static Map<String, CacheResult> resultCaches = new ConcurrentHashMap<>();
//	private static Map<String, CacheResult> resultCaches = Maps.newConcurrentMap();

	// 模拟AOP方式，到缓存中获取档案的数据信息，如果没有获取到，使用注入的ProceedingJoinPoint方法执行获取数据信息
	@SuppressWarnings("unchecked")
	public static <T> T get(String key, ProceedingJoinPoint<T> pjp){
		if(StringUtils.isBlank(key)){
			return pjp.proceed();
		}
		
		T object = null;
		CacheResult result = resultCaches.get(key);
		if(result == null){
			object = pjp.proceed();
			resultCaches.put(key, new ResultCacheManager().new CacheResult(object));
		} else {
			object = (T)result.getResult();
			logger.info("获取到缓存信息：{}", key);
		}
		return object;
	}
	
	// 定制spring-job.xml中定制的任务。用来删除过期的结果缓存
	public void deleteResultCache(long timeToLiveMinutes){
		//自己写过滤方法，不使用google的Maps的过滤方法，google的过滤方法返回的不是ConcurrentMap对象
		Set<String> resultKeys = new HashSet<String>();
		long now = new Date().getTime();
		for(String resultKey : resultCaches.keySet()){
			CacheResult result = resultCaches.get(resultKey);
			if(now - result.getCreateTime() > timeToLiveMinutes * 60 * 1000){
				resultKeys.add(resultKey);
			}
		}
		synchronized (resultCaches) {
			for(String resultKey : resultKeys){
				resultCaches.remove(resultKey);
			}
		}
		logger.info("清理掉{}个结果缓存", resultKeys.size());
	}
	

	public interface ProceedingJoinPoint<T> {
		public T proceed();
	}
	

	private class CacheResult {
		
		private Object result;
		private long createTime;
		
		public CacheResult(Object result){
			this.result = result;
			this.createTime = new Date().getTime();
		}

		public Object getResult() {
			return result;
		}

		public void setResult(Object result) {
			this.result = result;
		}

		public long getCreateTime() {
			return createTime;
		}

		public void setCreateTime(long createTime) {
			this.createTime = createTime;
		}
	}
	
}

