
package org.springframework.core.env;

// 是一个包含和暴露Enviroment引用的组件。所有的spring的application context都继承了EnvironmentCapable接口。
// Enviroment是干什么用的呢？
// Environment包含两方便的抽象，profile和property：
// profile的作用是，根据定义的profile，让哪一组Bean的定义生效，例如：用profile来定义不同环境的数据库连接。
// property是提供方便的抽象，应用程序可以方便的访问 system property 环境变量自定义属性等。
// 关于Environment的文章，有兴趣可以看一下：http://blog.csdn.NET/windsunmoon/article/details/45197361
public interface EnvironmentCapable {

	Environment getEnvironment();

}
