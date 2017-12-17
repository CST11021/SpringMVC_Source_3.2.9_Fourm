package com.whz.beanfactory;

import org.springframework.beans.factory.FactoryBean;

public class TestFactoryBean implements FactoryBean {

	public boolean isSingleton() {
        return true;
	}

	public Class getObjectType() {
		return String.class;
	}

	public Object getObject() {
		return "test";
	}

}