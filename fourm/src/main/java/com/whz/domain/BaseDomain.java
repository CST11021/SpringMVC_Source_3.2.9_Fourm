package com.whz.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 *<br><b>类描述:</b>
 *<pre>所示PO的父类</pre>
 *@see
 *@since
 */
/*实现了Serializable接口，以便JVM可以序列化PO实例*/
/*一般情况下，PO类最好都实现Serializable接口，这样JVM就能方便地将PO实例序列化到硬盘中，或者通过流的方式进行发送，为缓存、集群等功能带来便利。
* 我们往往需要将PO对象打印为一个字符串，这里我们用Apache的ToStringBuilder工具类来重写toString()方法*/
public class BaseDomain implements Serializable {
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
