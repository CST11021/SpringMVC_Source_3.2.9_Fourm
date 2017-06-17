package com.whz.utils;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.io.StringReader;

public abstract class Dom4jUtil {

    // 通过XML正文内容获取根节点
    public static Element getRootElementFromContent(String content) {
        StringReader is = new StringReader(content);
        SAXReader saxReader = new SAXReader();
        Document document;
        try {
            document = saxReader.read(is);
        } catch (DocumentException e) {
            throw new IllegalStateException(e);
        }
        return document.getRootElement();
    }

    // 通过输入流获取XML根节点
    public static Element getRootElementFromStream(InputStream is) {
        SAXReader saxReader = new SAXReader();
        Document document;
        try {
            document = saxReader.read(is);
        } catch (DocumentException e) {
            throw new IllegalStateException(e);
        }
        return document.getRootElement();
    }

    // 获取指定节点的属性值
    public static String getRequiredAttribute(Element node,String attributeName) {
        String result = node.attributeValue(attributeName);
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        throw new IllegalStateException("[" + node.getPath() + "]节点的[" + attributeName + "]属性值不能为空.");
    }

    // 获取指定节点的属性值
    public static String getAttribute(Element node, String attributeName) {
        return node.attributeValue(attributeName);
    }

    // 获得节点的属性值,如果为空,则返回默认值
    public static String getAttribute(Element node, String attributeName, String defaultValue) {
        String result = node.attributeValue(attributeName);
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        return defaultValue;
    }

    // 获得节点的属性值,并以整型的类型输出
    public static int getIntegerAttribute(Element node, String attributeName, int defaultValue) {
        String strValue = node.attributeValue(attributeName);
        if (StringUtils.isNotBlank(strValue)) {
            int value = 0;
            try{
                value = Integer.parseInt(strValue);
            }catch(NumberFormatException e){
                throw new IllegalStateException("[" + node.getPath() + "]节点的[" + attributeName + "]属性值不是整型类型.", e);
            }
            return value;
        }
        return defaultValue;
    }

    // 获得节点的属性值,并以布尔类型输出
    public static boolean getBooleanAttribute(Element node, String attributeName, boolean defaultValue) {
        String strValue = node.attributeValue(attributeName);
        if (StringUtils.isNotBlank(strValue)) {
            if (strValue.toLowerCase().equals("false")) {
                return false;
            } else if (strValue.toLowerCase().equals("true")) {
                return true;
            } else {
                throw new IllegalStateException("[" + node.getPath() + "]节点的[" + attributeName + "]属性值必须为false或者true.");
            }
        }
        return defaultValue;
    }

}

