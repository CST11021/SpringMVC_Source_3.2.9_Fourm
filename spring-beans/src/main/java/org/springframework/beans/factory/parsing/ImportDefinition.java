
package org.springframework.beans.factory.parsing;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

// 代表一个在解析进程中，已经被处理的<import>
public class ImportDefinition implements BeanMetadataElement {

	private final String importedResource;
	private final Resource[] actualResources;
	private final Object source;

	public ImportDefinition(String importedResource) {
		this(importedResource, null, null);
	}
	public ImportDefinition(String importedResource, Object source) {
		this(importedResource, null, source);
	}
	public ImportDefinition(String importedResource, Resource[] actualResources, Object source) {
		Assert.notNull(importedResource, "Imported resource must not be null");
		this.importedResource = importedResource;
		this.actualResources = actualResources;
		this.source = source;
	}


	public final String getImportedResource() {
		return this.importedResource;
	}
	public final Resource[] getActualResources() {
		return this.actualResources;
	}
	public final Object getSource() {
		return this.source;
	}

}
