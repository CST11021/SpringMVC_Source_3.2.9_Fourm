
package org.springframework.beans.factory.parsing;

import java.util.EventListener;

// Interface that receives callbacks for component, alias and import registrations during a bean definition reading process.
// 在读取BeanDefinition进程中注册组件、别名、import时的回调接口
public interface ReaderEventListener extends EventListener {

	// Notification that the given defaults has been registered.
	void defaultsRegistered(DefaultsDefinition defaultsDefinition);

	// 当解析完后一个Bean，并将BeanDefinition注册到注册表后，会来调用这个方法
	void componentRegistered(ComponentDefinition componentDefinition);

	// Notification that the given alias has been registered.
	void aliasRegistered(AliasDefinition aliasDefinition);

	// Notification that the given import has been processed.
	void importProcessed(ImportDefinition importDefinition);

}
