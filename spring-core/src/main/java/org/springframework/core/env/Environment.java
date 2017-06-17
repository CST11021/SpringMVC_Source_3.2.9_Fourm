
package org.springframework.core.env;

/**
 * Environment:应用上下文环境
 * 整个应用环境模型包括2个关键方面：
 *
 *  1、profiles配置组（以下简称组）：一个profile组，是一个以name名称命名的、逻辑上的、要被注册到容器中的BeanDefinition的集合。简单一点说，一个profile就代表一组BeanDefinition，
 *  这个对应配置文件中<beans profile="">。当加载解析xml配置文件的时候，只有active=true激活的BeanDefinition才会被加载进容器。

 * 2、properties环境变量：在几乎所有的应用中，Properties环境变量都扮演着非常重要的角色，且这些变量值可以来自于各种PropertySource属性源，如：properties文件、jvm虚拟机环境变量、
 * 操作系统环境变量、JNDI、Servlet上下文参数、自定义的属性对象、Map对象，等等。Environment环境对象为用户提供了方便的接口，用于配置和使用属性源。
 *
 */
public interface Environment extends PropertyResolver {

	// 获取当前环境对象激活的所有profile组。
	String[] getActiveProfiles();

	// 获取默认的profile组。如果当前环境对象中激活的组为空（getActiveProfiles()返回空数组）的话，则会启用默认profile组。
	String[] getDefaultProfiles();

	// 判断给定的一个或多个组中，是否存在满足当前环境对象配置的组（任意一个组满足即可）。如：
	// 调用acceptsProfiles("p1","!p2")，如果当前环境对象激活了p1，或者没有激活p2（注意是或，满足一个条件即可），则返回true，否则返回false。
	boolean acceptsProfiles(String... profiles);

}
