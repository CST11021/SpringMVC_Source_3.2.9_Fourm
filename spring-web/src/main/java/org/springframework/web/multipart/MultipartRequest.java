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

package org.springframework.web.multipart;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;

/**
 * This interface defines the multipart request access operations that are exposed for actual multipart requests.
 * It is extended by {@link MultipartHttpServletRequest} and the Portlet
 * {@link org.springframework.web.portlet.multipart.MultipartActionRequest}.
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 2.5.2
 */
// 实现该接口可以获取上传的文件的相关信息
public interface MultipartRequest {

	// 返回上传文件的所有组件名（一个请求可上传多个文件，通常使用<input type="file" name="file">这种组件，该方法返回的是组件名“file”）
	Iterator<String> getFileNames();

	// 根据上传的组件名获取一个 MultipartFile 对象
	MultipartFile getFile(String name);
	List<MultipartFile> getFiles(String name);

	// 组件名到MultipartFile的映射
	Map<String, MultipartFile> getFileMap();
	MultiValueMap<String, MultipartFile> getMultiFileMap();

	/**
	 * Determine the content type of the specified request part.
	 * @param paramOrFileName the name of the part
	 * @return the associated content type, or {@code null} if not defined
	 * @since 3.1
	 */
	String getMultipartContentType(String paramOrFileName);

}
