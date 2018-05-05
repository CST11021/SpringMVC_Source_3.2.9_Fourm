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

import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

/**
 * Represents an HTTP request or response entity, consisting of headers and body.
 *
 * <p>Typically used in combination with the {@link org.springframework.web.client.RestTemplate RestTemplate}, like so:
 * <pre class="code">
 * HttpHeaders headers = new HttpHeaders();
 * headers.setContentType(MediaType.TEXT_PLAIN);
 * HttpEntity&lt;String&gt; entity = new HttpEntity&lt;String&gt;(helloWorld, headers);
 * URI location = template.postForLocation("http://example.com", entity);
 * </pre>
 * or
 * <pre class="code">
 * HttpEntity&lt;String&gt; entity = template.getForEntity("http://example.com", String.class);
 * String body = entity.getBody();
 * MediaType contentType = entity.getHeaders().getContentType();
 * </pre>
 * Can also be used in Spring MVC, as a return value from a @Controller method:
 * <pre class="code">
 * &#64;RequestMapping("/handle")
 * public HttpEntity&lt;String&gt; handle() {
 *   HttpHeaders responseHeaders = new HttpHeaders();
 *   responseHeaders.set("MyResponseHeader", "MyValue");
 *   return new HttpEntity&lt;String&gt;("Hello World", responseHeaders);
 * }
 * </pre>
 *
 * @author Arjen Poutsma
 * @since 3.0.2
 * @see org.springframework.web.client.RestTemplate
 * @see #getBody()
 * @see #getHeaders()
 */
public class HttpEntity<T> {

	/** 表示一个空的报文主体 */
	public static final HttpEntity EMPTY = new HttpEntity();

	/** 表示报文头部分*/
	private final HttpHeaders headers;

	/** 表示报文主体对象 */
	private final T body;


	protected HttpEntity() {
		this(null, null);
	}
	public HttpEntity(T body) {
		this(body, null);
	}
	public HttpEntity(MultiValueMap<String, String> headers) {
		this(null, headers);
	}
	public HttpEntity(T body, MultiValueMap<String, String> headers) {
		this.body = body;
		HttpHeaders tempHeaders = new HttpHeaders();
		if (headers != null) {
			tempHeaders.putAll(headers);
		}
		this.headers = HttpHeaders.readOnlyHttpHeaders(tempHeaders);
	}


	// getter and setter ...
	public HttpHeaders getHeaders() {
		return this.headers;
	}
	public T getBody() {
		return this.body;
	}
	public boolean hasBody() {
		return (this.body != null);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof HttpEntity)) {
			return false;
		}
		HttpEntity<?> otherEntity = (HttpEntity<?>) other;
		return (ObjectUtils.nullSafeEquals(this.headers, otherEntity.headers) &&
				ObjectUtils.nullSafeEquals(this.body, otherEntity.body));
	}
	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(this.headers) * 29 + ObjectUtils.nullSafeHashCode(this.body);
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("<");
		if (body != null) {
			builder.append(body);
			if (headers != null) {
				builder.append(',');
			}
		}
		if (headers != null) {
			builder.append(headers);
		}
		builder.append('>');
		return builder.toString();
	}

}
