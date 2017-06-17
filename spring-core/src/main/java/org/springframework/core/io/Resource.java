
package org.springframework.core.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;


public interface Resource extends InputStreamSource {

	// 是否存在文件
	boolean exists();
	// 是否可读
	boolean isReadable();
	// 是否处于打开状态
	boolean isOpen();

	//提供不同资源到URL、URI和File类型的转换
	URL getURL() throws IOException;
	URI getURI() throws IOException;
	File getFile() throws IOException;

	// 获取该资源文件大小
	long contentLength() throws IOException;
	// 返回当前Resource代表的底层资源的最后修改时间
	long lastModified() throws IOException;
	// 用于创建相对于当前Resource代表的底层资源的资源，比如当前Resource代表文件资源“d:/abc/”则createRelative（“xyz.txt”）将返回表文件资源“d:/abc/xyz.txt”
	Resource createRelative(String relativePath) throws IOException;

	// 返回当前Resource代表的底层文件资源的文件路径，比如File资源“file://d:/test.txt”将返回“d:/test.txt”，而URL资源http://www.javass.cn将返回“”，因为只返回文件路径
	String getFilename();
	// 用于在错误信息处理中的打印信息
	String getDescription();

}
