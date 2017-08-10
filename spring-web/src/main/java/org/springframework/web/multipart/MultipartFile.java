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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A representation of an uploaded file received in a multipart request.
 *
 * <p>The file contents are either stored in memory or temporarily on disk.
 * In either case, the user is responsible for copying file contents to a
 * session-level or persistent store as and if desired. The temporary storages
 * will be cleared at the end of request processing.
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29.09.2003
 * @see org.springframework.web.multipart.MultipartHttpServletRequest
 * @see org.springframework.web.multipart.MultipartResolver
 */
public interface MultipartFile {

	// 获取表单中文件组件的名字
	String getName();

	// 获取上传文件的原名
	String getOriginalFilename();

	// 获取文件MIME类型，如image/pjpeg、text/plain等
	String getContentType();

	// 判断是否有上传文件
	boolean isEmpty();

	// 获取上传文件的字节大小，单位为byte
	long getSize();

	// 将文件的内容作为一个字节数组返回
	byte[] getBytes() throws IOException;

	// 获取上传文件的文件流
	InputStream getInputStream() throws IOException;

	// 将上传文件保存到一个目标文件中
	void transferTo(File dest) throws IOException, IllegalStateException;

}
