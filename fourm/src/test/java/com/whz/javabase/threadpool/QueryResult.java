package com.whz.javabase.threadpool;

import java.util.List;
import java.util.Map;

public class QueryResult {
	
	private String resId;
	private List<Map<String, String>> result;

	public QueryResult(String resId, List<Map<String, String>> result) {
		this.resId = resId;
		this.result = result;
	}

	public String getResId() {
		return resId;
	}
	public void setResId(String resId) {
		this.resId = resId;
	}
	public List<Map<String, String>> getResult() {
		return result;
	}
	public void setResult(List<Map<String, String>> result) {
		this.result = result;
	}
}

