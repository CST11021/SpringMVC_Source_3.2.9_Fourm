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

package org.springframework.http;

/**
 * Java 5 enumeration of HTTP request methods. Intended for use
 * with {@link org.springframework.http.client.ClientHttpRequest}
 * and {@link org.springframework.web.client.RestTemplate}.
 *
 * @author Arjen Poutsma
 * @since 3.0
 *
 * 表示HTTP的请求方法
 */
public enum HttpMethod {

	// 用与从服务器获取一份文档
	GET,
	// 向服务器发送需要处理的数据
	POST,
	// 只从服务器获取文档的首部
	HEAD,
	// 决定可以在服务器上执行哪些方法
	OPTIONS,
	// 将请求的主体部分存储在服务器上
	PUT,
	//
	PATCH,
	// 从服务器上删除一份文档
	DELETE,
	// 对可能经过代理服务器传送到服务器上去的报文进行追踪
	TRACE

}
