/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.core.io.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

// 对资源文件的编码进行处理，在getReader()方法中，当设置了编码属性后使用相应的编码作为输入流的编码
public class EncodedResource {

	private final Resource resource;
	private String encoding;
	private Charset charset;

	public EncodedResource(Resource resource) {
		Assert.notNull(resource, "Resource must not be null");
		this.resource = resource;
	}
	public EncodedResource(Resource resource, String encoding) {
		Assert.notNull(resource, "Resource must not be null");
		this.resource = resource;
		this.encoding = encoding;
	}
	public EncodedResource(Resource resource, Charset charset) {
		Assert.notNull(resource, "Resource must not be null");
		this.resource = resource;
		this.charset = charset;
	}


	// 当 encoding 或 charset 不为空时返回 TRUE
	public boolean requiresReader() {
		return (this.encoding != null || this.charset != null);
	}
	// 使用指定的编码（如果有的话）打开指定资源的读取器
	public Reader getReader() throws IOException {
		if (this.charset != null) {
			return new InputStreamReader(this.resource.getInputStream(), this.charset);
		}
		else if (this.encoding != null) {
			return new InputStreamReader(this.resource.getInputStream(), this.encoding);
		}
		else {
			return new InputStreamReader(this.resource.getInputStream());
		}
	}
	// 打开指定的资源一个输入流(没有特定编码的时候使用)
	public InputStream getInputStream() throws IOException {
		return this.resource.getInputStream();
	}


	// 属性读取器
	public final Resource getResource() {
		return this.resource;
	}
	public final String getEncoding() {
		return this.encoding;
	}
	public final Charset getCharset() {
		return this.charset;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof EncodedResource) {
			EncodedResource otherRes = (EncodedResource) obj;
			return (this.resource.equals(otherRes.resource) &&
					ObjectUtils.nullSafeEquals(this.encoding, otherRes.encoding));
		}
		return false;
	}
	@Override
	public int hashCode() {
		return this.resource.hashCode();
	}
	@Override
	public String toString() {
		return this.resource.toString();
	}

}
