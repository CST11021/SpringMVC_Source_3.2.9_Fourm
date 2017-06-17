package com.whz.service;

import com.whz.utils.Dom4jUtil;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@ManagedResource
public class ConfigServices {

	private Element rootElement;
	private String configLocation;
	private final static Logger logger = LoggerFactory.getLogger(ConfigServices.class);

	@ManagedOperation
	public void init(){
		InputStream is = null;
		try{
			File file = ResourceUtils.getFile(this.configLocation);
			is = new FileInputStream(file);
			rootElement = Dom4jUtil.getRootElementFromStream(is);
		} catch (Exception e) {
			logger.error("文件[{}]获取根节点出错",this.configLocation);
			e.getMessage();
		}finally{
			if(is!=null){
				try {
					is.close();
				} catch (IOException ioe) {
					ioe.getMessage();
				}
			}
		}
	}

	public Element getRootElement() {
		return rootElement;
	}
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}
}
