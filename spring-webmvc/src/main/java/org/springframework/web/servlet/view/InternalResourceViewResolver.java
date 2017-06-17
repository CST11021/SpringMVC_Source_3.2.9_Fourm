
package org.springframework.web.servlet.view;

import org.springframework.util.ClassUtils;

// 它是URLBasedViewResolver的子类，所以URLBasedViewResolver支持的特性它都支持。在实际应用中InternalResourceViewResolver也是使用的最广泛的一个视图解析器。

// 那么InternalResourceViewResolver有什么自己独有的特性呢？
// 单从字面意思来看，我们可以把InternalResourceViewResolver解释为内部资源视图解析器，这就是InternalResourceViewResolver的一个特性。
// InternalResourceViewResolver会把返回的视图名称都解析为InternalResourceView对象，InternalResourceView会把Controller处理器方法返回的模型属性都存放到对应的request属性中，
// 然后通过RequestDispatcher在服务器端把请求forword重定向到目标URL。比如在InternalResourceViewResolver中定义了prefix=/WEB-INF/，suffix=.jsp，然后请求的Controller处理器方法返回的视图名称为test，
// 那么这个时候InternalResourceViewResolver就会把test解析为一个InternalResourceView对象，先把返回的模型属性都存放到对应的HttpServletRequest属性中，然后利用RequestDispatcher在服务器端把请求forword到/WEB-INF/test.jsp。
// 这就是InternalResourceViewResolver一个非常重要的特性，我们都知道存放在/WEB-INF/下面的内容是不能直接通过request请求的方式请求到的，为了安全性考虑，我们通常会把jsp文件放在WEB-INF目录下，
// 而InternalResourceView在服务器端跳转的方式可以很好的解决这个问题。下面是一个InternalResourceViewResolver的定义，根据该定义当返回的逻辑视图名称是test的时候，
// InternalResourceViewResolver会给它加上定义好的前缀和后缀，组成“/WEB-INF/test.jsp”的形式，然后把它当做一个InternalResourceView的url新建一个InternalResourceView对象返回。
// <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">  
//   <property name="prefix" value="/WEB-INF/"/>  
//   <property name="suffix" value=".jsp"/>
// </bean>  
public class InternalResourceViewResolver extends UrlBasedViewResolver {

	private static final boolean jstlPresent = ClassUtils.isPresent("javax.servlet.jsp.jstl.core.Config", InternalResourceViewResolver.class.getClassLoader());

	private Boolean alwaysInclude;
	private Boolean exposeContextBeansAsAttributes;
	private String[] exposedContextBeanNames;



	public InternalResourceViewResolver() {
		Class viewClass = requiredViewClass();
		if (viewClass.equals(InternalResourceView.class) && jstlPresent) {
			viewClass = JstlView.class;
		}
		setViewClass(viewClass);
	}

	/**
	 * This resolver requires {@link InternalResourceView}.
	 */
	@Override
	protected Class requiredViewClass() {
		return InternalResourceView.class;
	}


	public void setAlwaysInclude(boolean alwaysInclude) {
		this.alwaysInclude = Boolean.valueOf(alwaysInclude);
	}
	public void setExposeContextBeansAsAttributes(boolean exposeContextBeansAsAttributes) {
		this.exposeContextBeansAsAttributes = exposeContextBeansAsAttributes;
	}
	public void setExposedContextBeanNames(String[] exposedContextBeanNames) {
		this.exposedContextBeanNames = exposedContextBeanNames;
	}


	@Override
	protected AbstractUrlBasedView buildView(String viewName) throws Exception {
		InternalResourceView view = (InternalResourceView) super.buildView(viewName);
		if (this.alwaysInclude != null) {
			view.setAlwaysInclude(this.alwaysInclude);
		}
		if (this.exposeContextBeansAsAttributes != null) {
			view.setExposeContextBeansAsAttributes(this.exposeContextBeansAsAttributes);
		}
		if (this.exposedContextBeanNames != null) {
			view.setExposedContextBeanNames(this.exposedContextBeanNames);
		}
		view.setPreventDispatchLoop(true);
		return view;
	}

}
