
package org.springframework.beans;

// 用于封装<bean>配置中的属性，比如：
// <bean id="car" class="com.whz.beanLifecycle.Car" init-method="myInit" destroy-method="myDestory" p:brand="红旗CA72" p:maxSpeed="200"/>
// PropertyValues 接口封装了 brand、maxSpeed 属性，每个属性又封装为 PropertyValue ，Car这个bean中可能还有其他属性，但是没有使用Spring的形式
// 注入，所以 PropertyValues 接口是获取不到这个属性的PropertyValue 的。

public interface PropertyValues {

	PropertyValue[] getPropertyValues();
	PropertyValue getPropertyValue(String propertyName);
	PropertyValues changesSince(PropertyValues old);
	boolean contains(String propertyName);
	boolean isEmpty();

}
