
package org.springframework.beans.factory.parsing;

// ReaderEventListener接口的空实现，所有回调方法都没有提供可执行操作
public class EmptyReaderEventListener implements ReaderEventListener {

	public void defaultsRegistered(DefaultsDefinition defaultsDefinition) {
		// no-op
	}

	public void componentRegistered(ComponentDefinition componentDefinition) {
		// no-op
	}

	public void aliasRegistered(AliasDefinition aliasDefinition) {
		// no-op
	}

	public void importProcessed(ImportDefinition importDefinition) {
		// no-op
	}

}
