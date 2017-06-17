
package org.springframework.util.xml;

import java.io.BufferedReader;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.util.StringUtils;

// Xml验证模式委托器
public class XmlValidationModeDetector {

	// 禁用验证
	public static final int VALIDATION_NONE = 0;
	// 自动检测验证模式
	public static final int VALIDATION_AUTO = 1;
	// 使用DTD验证模式
	public static final int VALIDATION_DTD = 2;
	// 使用XSD验证模式
	public static final int VALIDATION_XSD = 3;

	// 在一个XML文档的DTD声明时使用的标识符
	private static final String DOCTYPE = "DOCTYPE";


	// XML注释的开始
	private static final String START_COMMENT = "<!--";
	// XML注释的结束
	private static final String END_COMMENT = "-->";

	// 指示是否当前解析的位置是在XML注释
	private boolean inComment;


	// 自动检测验证模式的具体实现：检测验证模式的办法就是判断是否包含DOCTYPE，如果包含就是DTD，否则就是XSD。
	public int detectValidationMode(InputStream inputStream) throws IOException {
		// Peek into the file to look for DOCTYPE.
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		try {
			boolean isDtdValidated = false;
			String content;
			while ((content = reader.readLine()) != null) {
				// 如果读取的行是空或者是注释则略过
				content = consumeCommentTokens(content);
				if (this.inComment || !StringUtils.hasText(content)) {
					continue;
				}
				if (hasDoctype(content)) {
					isDtdValidated = true;
					break;
				}
				// 读取<开始符号，验证模式一定会在开始符号之前
				if (hasOpeningTag(content)) {
					// End of meaningful data...
					break;
				}
			}
			return (isDtdValidated ? VALIDATION_DTD : VALIDATION_XSD);
		}
		catch (CharConversionException ex) {
			// Choked on some character encoding...
			// Leave the decision up to the caller.
			return VALIDATION_AUTO;
		}
		finally {
			reader.close();
		}
	}
	// 判断文档内容包含DTD文档类型声明
	private boolean hasDoctype(String content) {
		return content.contains(DOCTYPE);
	}

	// content 中必须包含“<”，并且<不能再最后面，同时<后面的一个字符必须为字母时才返回 TRUE
	private boolean hasOpeningTag(String content) {
		// 如果当前解析的内容总是在注释内，则返回 false
		if (this.inComment) {
			return false;
		}
		int openTagIndex = content.indexOf('<');

		// Character.isLetter()确定指定的字符是否是一个字母
		return (openTagIndex > -1 &&
				(content.length() > openTagIndex + 1) &&
				Character.isLetter(content.charAt(openTagIndex + 1)));
	}




	/**
	 * Consumes all the leading comment data in the given String and returns the remaining content, which
	 * may be empty since the supplied content might be all comment data. For our purposes it is only important
	 * to strip leading comment content on a line since the first piece of non comment content will be either
	 * the DOCTYPE declaration or the root element of the document.
	 */
	private String consumeCommentTokens(String line) {
		if (!line.contains(START_COMMENT) && !line.contains(END_COMMENT)) {
			return line;
		}
		while ((line = consume(line)) != null) {
			if (!this.inComment && !line.trim().startsWith(START_COMMENT)) {
				return line;
			}
		}
		return line;
	}

	/**
	 * Consume the next comment token, update the "inComment" flag
	 * and return the remaining content.
	 */
	private String consume(String line) {
		int index = (this.inComment ? endComment(line) : startComment(line));
		return (index == -1 ? null : line.substring(index));
	}

	/**
	 * Try to consume the {@link #START_COMMENT} token.
	 * @see #commentToken(String, String, boolean)
	 */
	private int startComment(String line) {
		return commentToken(line, START_COMMENT, true);
	}

	private int endComment(String line) {
		return commentToken(line, END_COMMENT, false);
	}

	/**
	 * Try to consume the supplied token against the supplied content and update the
	 * in comment parse state to the supplied value. Returns the index into the content
	 * which is after the token or -1 if the token is not found.
	 */
	private int commentToken(String line, String token, boolean inCommentIfPresent) {
		int index = line.indexOf(token);
		if (index > - 1) {
			this.inComment = inCommentIfPresent;
		}
		return (index == -1 ? index : index + token.length());
	}

}
