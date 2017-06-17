
package org.springframework.web.servlet;

import java.util.Map;

import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;

public class ModelAndView {

	//代表一个视图对象，它有可能是String类型的视图名，也有可能就是一个试图对象
	private Object view;
	//用于存储渲染试图的数据
	private ModelMap model;
	//用于标识该 ModelAndView 是否已被清除
	private boolean cleared = false;


	public ModelAndView() {}
	public ModelAndView(String viewName) {
		this.view = viewName;
	}
	public ModelAndView(View view) {
		this.view = view;
	}
	public ModelAndView(String viewName, Map<String, ?> model) {
		this.view = viewName;
		if (model != null) {
			getModelMap().addAllAttributes(model);
		}
	}
	public ModelAndView(View view, Map<String, ?> model) {
		this.view = view;
		if (model != null) {
			getModelMap().addAllAttributes(model);
		}
	}
	public ModelAndView(String viewName, String modelName, Object modelObject) {
		this.view = viewName;
		addObject(modelName, modelObject);
	}
	public ModelAndView(View view, String modelName, Object modelObject) {
		this.view = view;
		addObject(modelName, modelObject);
	}


	public void setViewName(String viewName) {
		this.view = viewName;
	}
	public String getViewName() {
		return (this.view instanceof String ? (String) this.view : null);
	}
	public void setView(View view) {
		this.view = view;
	}
	public View getView() {
		return (this.view instanceof View ? (View) this.view : null);
	}

	protected Map<String, Object> getModelInternal() {
		return this.model;
	}
	public ModelMap getModelMap() {
		if (this.model == null) {
			this.model = new ModelMap();
		}
		return this.model;
	}
	public Map<String, Object> getModel() {
		return getModelMap();
	}

	public ModelAndView addObject(String attributeName, Object attributeValue) {
		getModelMap().addAttribute(attributeName, attributeValue);
		return this;
	}
	public ModelAndView addObject(Object attributeValue) {
		getModelMap().addAttribute(attributeValue);
		return this;
	}
	public ModelAndView addAllObjects(Map<String, ?> modelMap) {
		getModelMap().addAllAttributes(modelMap);
		return this;
	}


	public boolean wasCleared() {
		return (this.cleared && isEmpty());
	}
	//清除Model 和 View
	public void clear() {
		this.view = null;
		this.model = null;
		this.cleared = true;
	}
	//判断该 ModelAndView 的Model和View是否同时为空
	public boolean isEmpty() {
		return (this.view == null && CollectionUtils.isEmpty(this.model));
	}
	public boolean hasView() {
		return (this.view != null);
	}

	//判断该视图的类型，如果视图是引用视图名的String类型，则返回TRUE
	public boolean isReference() {
		return (this.view instanceof String);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ModelAndView: ");
		if (isReference()) {
			sb.append("reference to view with name '").append(this.view).append("'");
		}
		else {
			sb.append("materialized View is [").append(this.view).append(']');
		}
		sb.append("; model is ").append(this.model);
		return sb.toString();
	}

}
