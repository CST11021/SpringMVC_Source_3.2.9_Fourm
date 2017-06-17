
package org.springframework.beans.factory.parsing;

import java.util.EventListener;

// Interface that receives callbacks for component, alias and import registrations during a bean definition reading process.
// 在读取BeanDefinition进程中注册组件、别名、import时的回调接口
public interface ReaderEventListener extends EventListener {

	// Notification that the given defaults has been registered.
	void defaultsRegistered(DefaultsDefinition defaultsDefinition);

	// Notification that the given component has been registered.
	void componentRegistered(ComponentDefinition componentDefinition);

	// Notification that the given alias has been registered.
	void aliasRegistered(AliasDefinition aliasDefinition);

	// Notification that the given import has been processed.
	void importProcessed(ImportDefinition importDefinition);

}
