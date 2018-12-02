HttpSessionActivationListener	HttpSessionEvent
HttpSessionAttributeListener	HttpSessionBindingEvent
HttpSessionBindingListener		HttpSessionBindingEvent
HttpSessionListener 			HttpSessionEvent

ServletContextAttributeListener	ServletContextAttributeEvent
ServletContextListener 			ServletContextEvent

ServletRequestAttributeListener ServletRequestAttributeEvent
ServletRequestListener 			ServletRequestEvent

AsyncListener 					AsyncEvent



web.xml的加载顺序是：【Context-Param】->【Listener】->【Filter】->【Servlet】，而同个类型之间的实际程序调用的时候的顺序是根据对应的Mapping的顺序进行调用。