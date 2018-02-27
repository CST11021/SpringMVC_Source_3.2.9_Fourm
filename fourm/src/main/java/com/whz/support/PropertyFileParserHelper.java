package com.whz.support;

/**
 * Created by Administrator on 2016/5/17.
 */

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.Properties;

public class PropertyFileParserHelper extends PropertyPlaceholderConfigurer {
    private static Properties totalPropertyHolder = new Properties();

    public PropertyFileParserHelper() {}

    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        super.processProperties(beanFactoryToProcess, props);
        totalPropertyHolder.putAll(props);
    }

    public static String get(String name) {
        //有时候我们在property文件中配置加密后的信息，我们可以在这里进行解密操作
        return totalPropertyHolder.getProperty(name);
    }

    public static String get(String name, String defautValue) {
        return totalPropertyHolder.getProperty(name, defautValue);
    }
}
