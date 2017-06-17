
package org.springframework.beans.factory.parsing;

// SPI接口：允许工具和其他外部进程处理bean定义解析过程中报告的错误和警告。
public interface ProblemReporter {

	// 在解析过程中遇到致命错误时调用
	void fatal(Problem problem);

	// 在解析过程中遇到错误时调用。
	void error(Problem problem);

	// 在解析过程中遇到警告时调用。
	void warning(Problem problem);

}
