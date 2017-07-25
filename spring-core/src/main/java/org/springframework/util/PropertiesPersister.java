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

package org.springframework.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

/**
 * Strategy interface for persisting {@code java.util.Properties},
 * allowing for pluggable parsing strategies.
 *
 * <p>The default implementation is DefaultPropertiesPersister,
 * providing the native parsing of {@code java.util.Properties},
 * but allowing for reading from any Reader and writing to any Writer
 * (which allows to specify an encoding for a properties file).
 *
 * <p>As of Spring 1.2.2, this interface also supports properties XML files,
 * through the {@code loadFromXml} and {@code storeToXml} methods.
 * The default implementations delegate to JDK 1.5's corresponding methods.
 *
 * @author Juergen Hoeller
 * @since 10.03.2004
 * @see DefaultPropertiesPersister
 * @see java.util.Properties
 */
// Properties对象的持久化接口
public interface PropertiesPersister {

	// 从给定的InputStream加载属性到给定的属性对象。
	void load(Properties props, InputStream is) throws IOException;
	void load(Properties props, Reader reader) throws IOException;
	// 将给定属性对象的内容写入给定的OutputStream。
	void store(Properties props, OutputStream os, String header) throws IOException;
	void store(Properties props, Writer writer, String header) throws IOException;

	// 将给定的XML InputStream的属性加载到给定的properties对象中
	void loadFromXml(Properties props, InputStream is) throws IOException;
	// 将给定的属性对象的内容写入给定的XML OutputStream。
	void storeToXml(Properties props, OutputStream os, String header) throws IOException;
	void storeToXml(Properties props, OutputStream os, String header, String encoding) throws IOException;

}
