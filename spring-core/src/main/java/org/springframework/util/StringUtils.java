
package org.springframework.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;


public abstract class StringUtils {

	private static final String FOLDER_SEPARATOR = "/";
	private static final String WINDOWS_FOLDER_SEPARATOR = "\\";
	private static final String TOP_PATH = "..";
	private static final String CURRENT_PATH = ".";
	private static final char EXTENSION_SEPARATOR = '.';


	// General convenience methods for working with Strings（使用字符串的一般便利方法）

	// 对象不为空，字符串不为空串
	public static boolean isEmpty(Object str) {
		return (str == null || "".equals(str));
	}

	// null 或 "" 返回 false
	public static boolean hasLength(CharSequence str) {
		return (str != null && str.length() > 0);
	}
	public static boolean hasLength(String str) {
		return hasLength((CharSequence) str);
	}

	//** StringUtils.hasText(null) = false
	// * StringUtils.hasText("") = false
	// * StringUtils.hasText(" ") = false
	// * StringUtils.hasText("12345") = true
	// * StringUtils.hasText(" 12345 ") = true
	// */
	public static boolean hasText(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}
	public static boolean hasText(String str) {
		return hasText((CharSequence) str);
	}

	// 判断字符串是否包含空格
	public static boolean containsWhitespace(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}
	public static boolean containsWhitespace(String str) {
		return containsWhitespace((CharSequence) str);
	}

	// 剔除空格
	public static String trimWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	public static String trimAllWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		int index = 0;
		while (sb.length() > index) {
			if (Character.isWhitespace(sb.charAt(index))) {
				sb.deleteCharAt(index);
			}
			else {
				index++;
			}
		}
		return sb.toString();
	}
	public static String trimLeadingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}
	public static String trimTrailingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	//剔除指定字符
	public static String trimLeadingCharacter(String str, char leadingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(0) == leadingCharacter) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}
	public static String trimTrailingCharacter(String str, char trailingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(sb.length() - 1) == trailingCharacter) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	// 前/后缀校验
	public static boolean startsWithIgnoreCase(String str, String prefix) {
		if (str == null || prefix == null) {
			return false;
		}
		if (str.startsWith(prefix)) {
			return true;
		}
		if (str.length() < prefix.length()) {
			return false;
		}
		String lcStr = str.substring(0, prefix.length()).toLowerCase();
		String lcPrefix = prefix.toLowerCase();
		return lcStr.equals(lcPrefix);
	}
	public static boolean endsWithIgnoreCase(String str, String suffix) {
		if (str == null || suffix == null) {
			return false;
		}
		if (str.endsWith(suffix)) {
			return true;
		}
		if (str.length() < suffix.length()) {
			return false;
		}

		String lcStr = str.substring(str.length() - suffix.length()).toLowerCase();
		String lcSuffix = suffix.toLowerCase();
		return lcStr.equals(lcSuffix);
	}

	//测试给定字符串是否匹配给定字符串给定索引处
	public static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
		for (int j = 0; j < substring.length(); j++) {
			int i = index + j;
			if (i >= str.length() || str.charAt(i) != substring.charAt(j)) {
				return false;
			}
		}
		return true;
	}

	// 计算子串个数
	public static int countOccurrencesOf(String str, String sub) {
		if (str == null || sub == null || str.length() == 0 || sub.length() == 0) {
			return 0;
		}
		int count = 0;
		int pos = 0;
		int idx;
		while ((idx = str.indexOf(sub, pos)) != -1) {
			++count;
			pos = idx + sub.length();
		}
		return count;
	}

	// 字符串替换
	public static String replace(String inString, String oldPattern, String newPattern) {
		if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
			return inString;
		}
		StringBuilder sb = new StringBuilder();
		int pos = 0; // our position in the old string
		int index = inString.indexOf(oldPattern);
		// the index of an occurrence we've found, or -1
		int patLen = oldPattern.length();
		while (index >= 0) {
			sb.append(inString.substring(pos, index));
			sb.append(newPattern);
			pos = index + patLen;
			index = inString.indexOf(oldPattern, pos);
		}
		sb.append(inString.substring(pos));
		// remember to append any characters to the right of a match
		return sb.toString();
	}

	// 删除操作
	public static String delete(String inString, String pattern) {
		return replace(inString, pattern, "");
	}
	public static String deleteAny(String inString, String charsToDelete) {
		if (!hasLength(inString) || !hasLength(charsToDelete)) {
			return inString;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < inString.length(); i++) {
			char c = inString.charAt(i);
			if (charsToDelete.indexOf(c) == -1) {
				sb.append(c);
			}
		}
		return sb.toString();
	}


	// Convenience methods for working with formatted Strings(使用格式化字符串的便捷方法)

	// "myString" ----> "'myString'"
	public static String quote(String str) {
		return (str != null ? "'" + str + "'" : null);
	}
	public static Object quoteIfString(Object obj) {
		return (obj instanceof String ? quote((String) obj) : obj);
	}

	// 从最后一个“.”开始截取字符串，如："i.am.not.unqualified" ---> "unqualified"
	public static String unqualify(String qualifiedName) {
		return unqualify(qualifiedName, '.');
	}
	// 指定一个字符截取
	public static String unqualify(String qualifiedName, char separator) {
		return qualifiedName.substring(qualifiedName.lastIndexOf(separator) + 1);
	}

	// 首字符大写
	public static String capitalize(String str) {
		return changeFirstCharacterCase(str, true);
	}
	// 首字符小写
	public static String uncapitalize(String str) {
		return changeFirstCharacterCase(str, false);
	}
	// 首字符大小写转换
	private static String changeFirstCharacterCase(String str, boolean capitalize) {
		if (str == null || str.length() == 0) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str.length());
		if (capitalize) {
			sb.append(Character.toUpperCase(str.charAt(0)));
		}
		else {
			sb.append(Character.toLowerCase(str.charAt(0)));
		}
		sb.append(str.substring(1));
		return sb.toString();
	}

	// e.g. "mypath/myfile.txt" -> "myfile.txt".
	public static String getFilename(String path) {
		if (path == null) {
			return null;
		}
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
	}
	// e.g. "mypath/myfile.txt" -> "txt".
	public static String getFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		if (extIndex == -1) {
			return null;
		}
		int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (folderIndex > extIndex) {
			return null;
		}
		return path.substring(extIndex + 1);
	}
	// e.g. "mypath/myfile.txt" -> "mypath/myfile".
	public static String stripFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		if (extIndex == -1) {
			return path;
		}
		int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (folderIndex > extIndex) {
			return path;
		}
		return path.substring(0, extIndex);
	}

	// 以path作为相对路径relativePath的根路径返回
	public static String applyRelativePath(String path, String relativePath) {
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (separatorIndex != -1) {
			String newPath = path.substring(0, separatorIndex);
			if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
				newPath += FOLDER_SEPARATOR;
			}
			return newPath + relativePath;
		}
		else {
			return relativePath;
		}
	}

	//	assertEquals("mypath/myfile", StringUtils.cleanPath("mypath/myfile"));
	//	assertEquals("mypath/myfile", StringUtils.cleanPath("mypath\\myfile"));
	//	assertEquals("mypath/myfile", StringUtils.cleanPath("mypath/../mypath/myfile"));
	//	assertEquals("mypath/myfile", StringUtils.cleanPath("mypath/myfile/../../mypath/myfile"));
	//	assertEquals("../mypath/myfile", StringUtils.cleanPath("../mypath/myfile"));
	//	assertEquals("../mypath/myfile", StringUtils.cleanPath("../mypath/../mypath/myfile"));
	//	assertEquals("../mypath/myfile", StringUtils.cleanPath("mypath/../../mypath/myfile"));
	//	assertEquals("/../mypath/myfile", StringUtils.cleanPath("/../mypath/myfile"));
	//	assertEquals("/mypath/myfile", StringUtils.cleanPath("/a/:b/../../mypath/myfile"));
	//	assertEquals("file:///c:/path/to/the%20file.txt", StringUtils.cleanPath("file:///c:/some/../path/to/the%20file.txt"));
	public static String cleanPath(String path) {
		if (path == null) {
			return null;
		}
		String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

		// Strip prefix from path to analyze, to not treat it as part of the
		// first path element. This is necessary to correctly parse paths like
		// "file:core/../core/io/Resource.class", where the ".." should just
		// strip the first "core" directory while keeping the "file:" prefix.
		int prefixIndex = pathToUse.indexOf(":");
		String prefix = "";
		if (prefixIndex != -1) {
			prefix = pathToUse.substring(0, prefixIndex + 1);
			if (prefix.contains("/")) {
				prefix = "";
			}
			else {
				pathToUse = pathToUse.substring(prefixIndex + 1);
			}
		}
		if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
			prefix = prefix + FOLDER_SEPARATOR;
			pathToUse = pathToUse.substring(1);
		}

		String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
		List<String> pathElements = new LinkedList<String>();
		int tops = 0;

		for (int i = pathArray.length - 1; i >= 0; i--) {
			String element = pathArray[i];
			if (CURRENT_PATH.equals(element)) {
				// Points to current directory - drop it.
			}
			else if (TOP_PATH.equals(element)) {
				// Registering top path found.
				tops++;
			}
			else {
				if (tops > 0) {
					// Merging path element with element corresponding to top path.
					tops--;
				}
				else {
					// Normal path element found.
					pathElements.add(0, element);
				}
			}
		}

		// Remaining top paths need to be retained.
		for (int i = 0; i < tops; i++) {
			pathElements.add(0, TOP_PATH);
		}

		return prefix + collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
	}

	// 使用 cleanPath() 转换后在做字符串比对
	public static boolean pathEquals(String path1, String path2) {
		return cleanPath(path1).equals(cleanPath(path2));
	}

	/**
	 * Parse the given {@code localeString} value into a {@link Locale}.
	 * <p>This is the inverse operation of {@link Locale#toString Locale's toString}.
	 * @param localeString the locale String, following {@code Locale's}
	 * {@code toString()} format ("en", "en_UK", etc);
	 * also accepts spaces as separators, as an alternative to underscores
	 * @return a corresponding {@code Locale} instance
	 * @throws IllegalArgumentException in case of an invalid locale specification
	 */
	public static Locale parseLocaleString(String localeString) {
		String[] parts = tokenizeToStringArray(localeString, "_ ", false, false);
		String language = (parts.length > 0 ? parts[0] : "");
		String country = (parts.length > 1 ? parts[1] : "");
		validateLocalePart(language);
		validateLocalePart(country);
		String variant = "";
		if (parts.length > 2) {
			// There is definitely a variant, and it is everything after the country
			// code sans the separator between the country code and the variant.
			int endIndexOfCountryCode = localeString.indexOf(country, language.length()) + country.length();
			// Strip off any leading '_' and whitespace, what's left is the variant.
			variant = trimLeadingWhitespace(localeString.substring(endIndexOfCountryCode));
			if (variant.startsWith("_")) {
				variant = trimLeadingCharacter(variant, '_');
			}
		}
		return (language.length() > 0 ? new Locale(language, country, variant) : null);
	}

	private static void validateLocalePart(String localePart) {
		for (int i = 0; i < localePart.length(); i++) {
			char ch = localePart.charAt(i);
			if (ch != '_' && ch != ' ' && !Character.isLetterOrDigit(ch)) {
				throw new IllegalArgumentException("Locale part \"" + localePart + "\" contains invalid characters");
			}
		}
	}

	// RFC 3066 兼容标记语言，运用于HTTP "Accept-Language" header.
	public static String toLanguageTag(Locale locale) {
		return locale.getLanguage() + (hasText(locale.getCountry()) ? "-" + locale.getCountry() : "");
	}



	// Convenience methods for working with String arrays（使用字符串数组的便捷方法）

	// 向array添加str
	public static String[] addStringToArray(String[] array, String str) {
		if (ObjectUtils.isEmpty(array)) {
			return new String[] {str};
		}
		String[] newArr = new String[array.length + 1];
		System.arraycopy(array, 0, newArr, 0, array.length);
		newArr[array.length] = str;
		return newArr;
	}
	// 合并两个数组
	public static String[] concatenateStringArrays(String[] array1, String[] array2) {
		if (ObjectUtils.isEmpty(array1)) {
			return array2;
		}
		if (ObjectUtils.isEmpty(array2)) {
			return array1;
		}
		String[] newArr = new String[array1.length + array2.length];
		System.arraycopy(array1, 0, newArr, 0, array1.length);
		System.arraycopy(array2, 0, newArr, array1.length, array2.length);
		return newArr;
	}
	// 合并两个数组，去重
	public static String[] mergeStringArrays(String[] array1, String[] array2) {
		if (ObjectUtils.isEmpty(array1)) {
			return array2;
		}
		if (ObjectUtils.isEmpty(array2)) {
			return array1;
		}
		List<String> result = new ArrayList<String>();
		result.addAll(Arrays.asList(array1));
		for (String str : array2) {
			if (!result.contains(str)) {
				result.add(str);
			}
		}
		return toStringArray(result);
	}

	// 数组排序
	public static String[] sortStringArray(String[] array) {
		if (ObjectUtils.isEmpty(array)) {
			return new String[0];
		}
		Arrays.sort(array);
		return array;
	}

	// 转换为 String[]
	public static String[] toStringArray(Collection<String> collection) {
		if (collection == null) {
			return null;
		}
		return collection.toArray(new String[collection.size()]);
	}
	public static String[] toStringArray(Enumeration<String> enumeration) {
		if (enumeration == null) {
			return null;
		}
		List<String> list = Collections.list(enumeration);
		return list.toArray(new String[list.size()]);
	}

	// 剔除数组中的空串
	public static String[] trimArrayElements(String[] array) {
		if (ObjectUtils.isEmpty(array)) {
			return new String[0];
		}
		String[] result = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			String element = array[i];
			result[i] = (element != null ? element.trim() : null);
		}
		return result;
	}

	// String[] 去重
	public static String[] removeDuplicateStrings(String[] array) {
		if (ObjectUtils.isEmpty(array)) {
			return array;
		}
		Set<String> set = new TreeSet<String>();
		for (String element : array) {
			set.add(element);
		}
		return toStringArray(set);
	}

	// 将字符串以 delimiter 作为分隔符，拆分为两个字符串
	public static String[] split(String toSplit, String delimiter) {
		if (!hasLength(toSplit) || !hasLength(delimiter)) {
			return null;
		}
		int offset = toSplit.indexOf(delimiter);
		if (offset < 0) {
			return null;
		}
		String beforeDelimiter = toSplit.substring(0, offset);
		String afterDelimiter = toSplit.substring(offset + delimiter.length());
		return new String[] {beforeDelimiter, afterDelimiter};
	}

	// 以 delimiter 作为分隔符，将数组中的String转为 Properties 对象
	public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter) {
		return splitArrayElementsIntoProperties(array, delimiter, null);
	}
	public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter, String charsToDelete) {

		if (ObjectUtils.isEmpty(array)) {
			return null;
		}
		Properties result = new Properties();
		for (String element : array) {
			if (charsToDelete != null) {
				element = deleteAny(element, charsToDelete);
			}
			String[] splittedElement = split(element, delimiter);
			if (splittedElement == null) {
				continue;
			}
			result.setProperty(splittedElement[0].trim(), splittedElement[1].trim());
		}
		return result;
	}

	// StringUtils.tokenizeToStringArray("a,b , ,c", ","); -----------> ["a","b","c"]
	public static String[] tokenizeToStringArray(String str, String delimiters) {
		return tokenizeToStringArray(str, delimiters, true, true);
	}
	public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

		if (str == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0) {
				tokens.add(token);
			}
		}
		return toStringArray(tokens);
	}

	public static String[] delimitedListToStringArray(String str, String delimiter) {
		return delimitedListToStringArray(str, delimiter, null);
	}
	public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {
		if (str == null) {
			return new String[0];
		}
		if (delimiter == null) {
			return new String[] {str};
		}
		List<String> result = new ArrayList<String>();
		if ("".equals(delimiter)) {
			for (int i = 0; i < str.length(); i++) {
				result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
			}
		}
		else {
			int pos = 0;
			int delPos;
			while ((delPos = str.indexOf(delimiter, pos)) != -1) {
				result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
				pos = delPos + delimiter.length();
			}
			if (str.length() > 0 && pos <= str.length()) {
				// Add rest of String, but not in case of empty input.
				result.add(deleteAny(str.substring(pos), charsToDelete));
			}
		}
		return toStringArray(result);
	}

	// 将CSV列表转换成字符串数组
	public static String[] commaDelimitedListToStringArray(String str) {
		return delimitedListToStringArray(str, ",");
	}
	public static Set<String> commaDelimitedListToSet(String str) {
		Set<String> set = new TreeSet<String>();
		String[] tokens = commaDelimitedListToStringArray(str);
		for (String token : tokens) {
			set.add(token);
		}
		return set;
	}

	public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix) {
		if (CollectionUtils.isEmpty(coll)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Iterator<?> it = coll.iterator();
		while (it.hasNext()) {
			sb.append(prefix).append(it.next()).append(suffix);
			if (it.hasNext()) {
				sb.append(delim);
			}
		}
		return sb.toString();
	}
	public static String collectionToDelimitedString(Collection<?> coll, String delim) {
		return collectionToDelimitedString(coll, delim, "", "");
	}
	public static String collectionToCommaDelimitedString(Collection<?> coll) {
		return collectionToDelimitedString(coll, ",");
	}

	public static String arrayToDelimitedString(Object[] arr, String delim) {
		if (ObjectUtils.isEmpty(arr)) {
			return "";
		}
		if (arr.length == 1) {
			return ObjectUtils.nullSafeToString(arr[0]);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) {
				sb.append(delim);
			}
			sb.append(arr[i]);
		}
		return sb.toString();
	}
	public static String arrayToCommaDelimitedString(Object[] arr) {
		return arrayToDelimitedString(arr, ",");
	}

}
