/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.web.multipart.support;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Abstract base implementation of the MultipartHttpServletRequest interface.
 * Provides management of pre-generated MultipartFile instances.
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 06.10.2003
 */
public abstract class AbstractMultipartHttpServletRequest extends HttpServletRequestWrapper implements MultipartHttpServletRequest {

	// 表示上传文件请求的parameter信息，MultipartFile 用于表示上传的文件
	private MultiValueMap<String, MultipartFile> multipartFiles;


	protected AbstractMultipartHttpServletRequest(HttpServletRequest request) {
		super(request);
	}

	// 重写父类方法，返回一个 HttpServletRequest 类型的请求对象
	@Override
	public HttpServletRequest getRequest() {
		return (HttpServletRequest) super.getRequest();
	}
	// 获取请求方法
	public HttpMethod getRequestMethod() {
		return HttpMethod.valueOf(getRequest().getMethod());
	}
	// 获取请求首部
	public HttpHeaders getRequestHeaders() {
		HttpHeaders headers = new HttpHeaders();

		Enumeration<String> headerNames = getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			headers.put(headerName, Collections.list(getHeaders(headerName)));
		}
		return headers;
	}
	// 获取上传的文件名
	public Iterator<String> getFileNames() {
		return getMultipartFiles().keySet().iterator();
	}
	// 根据表单的组件名获取第一个文件对象 MultipartFile
	public MultipartFile getFile(String name) {
		return getMultipartFiles().getFirst(name);
	}
	// 根据表单的组件名获取一组文件对象 MultipartFile ，一次可能上传多个文件的情况
	public List<MultipartFile> getFiles(String name) {
		List<MultipartFile> multipartFiles = getMultipartFiles().get(name);
		if (multipartFiles != null) {
			return multipartFiles;
		}
		else {
			return Collections.emptyList();
		}
	}
	// 组件名到MultipartFile的映射
	public Map<String, MultipartFile> getFileMap() {
		return getMultipartFiles().toSingleValueMap();
	}
	// 组件名到MultipartFile的映射
	public MultiValueMap<String, MultipartFile> getMultiFileMap() {
		return getMultipartFiles();
	}


	protected void initializeMultipart() {
		throw new IllegalStateException("Multipart request not initialized");
	}
	protected MultiValueMap<String, MultipartFile> getMultipartFiles() {
		if (this.multipartFiles == null) {
			initializeMultipart();
		}
		return this.multipartFiles;
	}
	protected final void setMultipartFiles(MultiValueMap<String, MultipartFile> multipartFiles) {
		this.multipartFiles = new LinkedMultiValueMap<String, MultipartFile>(Collections.unmodifiableMap(multipartFiles));
	}

}
