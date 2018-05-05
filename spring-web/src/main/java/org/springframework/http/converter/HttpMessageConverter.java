/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.http.converter;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

/**
 * 一个特殊的策略接口，用于将HTTP请求和响应数据转化为对应的对象类型<T>
 * HttpMessageConverter<T> 是 Spring3.0 新添加的一个接口，负责将请求信息转换为一个对象（类型为 T），将对象（类型为 T）输出为响应信息
 *
 * Strategy interface that specifies a converter that can convert from and to HTTP requests and responses.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface HttpMessageConverter<T> {

	/**
	 * 判断这个转换器是否可以读取对应的对象类型，即转换器是否可将请求信息转换为 clazz 类型的对象，
	 * 入参 mediaType 表示该转换器支持的 MIME 类型(text/html,applaiction/json等)
	 */
	boolean canRead(Class<?> clazz, MediaType mediaType);

	/**
	 * 判断转换器是否可将 clazz 类型的对象写到响应流中，
	 * 入参 mediaType 表示响应流支持的媒体类型
	 */
	boolean canWrite(Class<?> clazz, MediaType mediaType);

	/** 获取该转换器支持的媒体类型 */
	List<MediaType> getSupportedMediaTypes();

	/** 将请求信息流转换为 T 类型的对象 */
	T read(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException;

	/** 将 T 类型的对象写到响应流中，同时指定该响应流相应的媒体类型为 contentType */
	void write(T t, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException;

}
