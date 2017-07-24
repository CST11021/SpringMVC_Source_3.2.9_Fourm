/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context;

import java.util.Locale;

/**
 * Strategy interface for resolving messages, with support for the parameterization and internationalization of such messages.
 *
 * <p>Spring provides two out-of-the-box implementations for production:
 * <ul>
 * <li>{@link org.springframework.context.support.ResourceBundleMessageSource},
 * built on top of the standard {@link java.util.ResourceBundle}
 * <li>{@link org.springframework.context.support.ReloadableResourceBundleMessageSource},
 * being able to reload message definitions without restarting the VM
 * </ul>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.context.support.ResourceBundleMessageSource
 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
 */
// 解决消息的策略接口，支持参数化和此类消息的国际化
// 为应用提供i18n国际化消息访问的功能。
// Spring目前提供了两个MessageSource的实现:ResourceBundleMessageSource和StaticMessageSource。
// 它们都继承NestingMessageSource以便能够处理嵌套的消息。StaticMessageSource很少被使用，但能以编程的方式向消息源添加消息。
public interface MessageSource {

	// 用来从MessageSource获取消息的基本方法。如果在指定的locale中没有找到消息，则使用默认的消息。args中的参数将使用标准类库中的MessageFormat来作消息中替换值。
	String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

	// 本质上和上一个方法相同，其区别在：没有指定默认值，如果没找到消息，会抛出一个NoSuchMessageException异常。
	String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;

	// 上面方法中所使用的属性都封装到一个MessageSourceResolvable实现中，而本方法可以指定MessageSourceResolvable实现。
	String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;

}
/**
	当一个ApplicationContext被加载时，它会自动在context中查找已定义为MessageSource类型的bean。此bean的名称须为messageSource。
 如果找到，那么所有对上述方法的调用将被委托给该bean。否则ApplicationContext会在其父类中查找是否含有同名的bean。如果有，就把它
 作为MessageSource。如果它最终没有找到任何的消息源，一个空的StaticMessageSource将会被实例化，使它能够接受上述方法的调用。

	ResourceBundleMessageSource会用得更多一些，为此提供了一下示例：
 <beans>
	 <bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
		 <property name="basenames">
			 <list>
				 <value>format</value>
				 <value>exceptions</value>
				 <value>windows</value>
			 </list>
		 </property>
	 </bean>
 </beans>
 	这段配置假定在你的classpath中有三个资源文件（resource bundle），它们是format，exceptions和windows。通过ResourceBundle，使用
 JDK中解析消息的标准方式，来处理任何解析消息的请求。出于示例的目的，假定上面的两个资源文件的内容为…

 # in 'format.properties'
 message=Alligators rock!
 # in 'exceptions.properties'
 argument.required=The '{0}' argument is required.
 下面是测试代码。因为ApplicationContext实现也都实现了MessageSource接口，所以能被转型为MessageSource接口

 public static void main(String[] args) {
	 MessageSource resources = new ClassPathXmlApplicationContext("beans.xml");
	 String message = resources.getMessage("message", null, "Default", null);
	 System.out.println(message);
 }
 上述程序的输出结果将会是...
 Alligators rock!

 	总而言之，我们在'beans.xml'的文件中（在classpath根目录下）定义了一个messageSource bean，通过它的basenames属性引用多个资源文件；
 而basenames属性值由list元素所指定的三个值传入，它们以文件的形式存在并被放置在classpath的根目录下（分别为format.properties，exceptions.properties和windows.properties）。




 再分析个例子，这次我们将着眼于传递参数给查找的消息，这些参数将被转换为字符串并插入到已查找到的消息中的占位符（译注：资源文件中花括号里的数字即为占位符）。

 <beans>

 <!-- this MessageSource is being used in a web application -->
 <bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
 <property name="baseName" value="WEB-INF/test-messages"/>
 </bean>

 <!-- let's inject the above MessageSource into this POJO -->
 <bean id="example" class="com.foo.Example">
 <property name="messages" ref="messageSource"/>
 </bean>

 </beans>

 public class Example {
 private MessageSource messages;

 public void setMessages(MessageSource messages) {
 this.messages = messages;
 }

 public void execute() {
 String message = this.messages.getMessage("argument.required", new Object [] {"userDao"}, "Required", null);
 System.out.println(message);
 }

 }
 调用execute()方法的输出结果是...
 The 'userDao' argument is required.

 对于国际化（i18n），Spring中不同的MessageResource实现与JDK标准ResourceBundle中的locale解析规则一样。比如在上面例子中定义的messageSource bean，如果你想解析British (en-GB) locale的消息，那么需要创建format_en_GB.properties，exceptions_en_GB.properties和windows_en_GB.properties三个资源文件。
 Locale解析通常由应用程序根据运行环境来指定。出于示例的目的，我们对将要处理的(British)消息手工指定locale参数值。

 # in 'exceptions_en_GB.properties'
 argument.required=Ebagum lad, the '{0}' argument is required, I say, required.

 public static void main(final String[] args) {
 MessageSource resources = new ClassPathXmlApplicationContext("beans.xml");
 String message = resources.getMessage("argument.required",
 new Object [] {"userDao"}, "Required", Locale.UK);
 System.out.println(message);
 }
 上述程序运行时的输出结果是...
 Ebagum lad, the 'userDao' argument is required, I say, required.

 MessageSourceAware接口还能用于获取任何已定义的MessageSource引用。任何实现了MessageSourceAware接口的bean将在创建和配置的时候与MessageSource一同被注入。

 */