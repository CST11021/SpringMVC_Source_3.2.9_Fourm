package com.whz.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


//对象反射操作类
public class ObjectReflectUtil {

	private static final Logger logger = Logger.getLogger(ObjectReflectUtil.class);

	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * @param date 字符类型转日期类型
	 * @return 返回yyyy-MM-dd 格式日期类型
	 */
	private static Date getTheTime(String date) throws ParseException {
		Date cDate = null;
		cDate = format.parse(date);
		Date dd = new java.sql.Date(cDate.getTime());
		return dd;
	}

	/**
	 * 获取对象属性数组
	 * @param object Object 对象
	 * @return Field[] 属性数组
	 */
	public static Field[] getProperties(Object object) {
		if (object == null) {
			return null;
		}
		Field[] fields = object.getClass().getDeclaredFields();
		return fields;
	}

	/**
	 * 对象属性赋值
	 * @param object Object 字符类型转日期类型
	 * @param property String 对象属性
	 * @param valueObject String 赋值对象
	 */
	public static void setPropertyValue(Object object, String property, Object valueObject) {
		if (object != null && property != null && valueObject != null) {
			try {
				String value = valueObject.toString();
				PropertyDescriptor pd = new PropertyDescriptor(property, object.getClass());
				Method setMethod = pd.getWriteMethod();
				Class<?> propertyType = pd.getPropertyType();
				if (propertyType.equals(Integer.class) || propertyType.getName().equals("int")) { // 整型
					setMethod.invoke(object, new Object[] { new Integer(value) });
				} else if (propertyType.equals(String.class)) { // 字符串
					setMethod.invoke(object, new Object[] { value });
				} else if (propertyType.equals(Date.class)) { // utilDate
					setMethod.invoke(object, new Object[] { getTheTime(value) });
				} else if (propertyType.equals(java.sql.Date.class)) { // sqlDate
					setMethod.invoke(object, new Object[] { getTheTime(value) });
				} else if (propertyType.equals(Float.class)) {
					try {
						setMethod.invoke(object, new Object[] { new Float(value) });
					} catch (NumberFormatException e) {
						logger.debug("格式化Float出错:" + value);
					}
				} else {
					logger.debug("不支持的属性类型：" + propertyType);
				}
			} catch (IntrospectionException e) {
				logger.debug("初始化属性：" + property + "写入方法出错;" + e.getMessage());
			} catch (IllegalArgumentException e) {
				logger.debug("初始化属性：" + valueObject.getClass() + "." + property + "出错;" + e.getMessage());
			} catch (IllegalAccessException e) {
				logger.debug("初始化属性：" + valueObject.getClass() + "." + property + "出错;" + e.getMessage());
			} catch (InvocationTargetException e) {
				logger.debug("初始化属性：" + valueObject.getClass() + "." + property + "出错;" + e.getMessage());
			} catch (ParseException e) {
				logger.debug("初始化属性：" + valueObject.getClass() + "." + property + "出错;" + e.getMessage());
			}
		} else {
			logger.debug("参数出错");
		}
	}

	/**
	 * 利用反射方法构造对应的Class实例
	 * @param packageObject String 包.类
	 * @return Object
	 * @author:chenxy
	 */
	public static Object checkObject(String packageObject) {
		Object object = null;
		try {
			object = Class.forName(packageObject).newInstance();
			return object;
		} catch (ClassNotFoundException e) {
			logger.debug("类未找到ClassNotFoundException：" + e.getMessage());
			return null;
		} catch (InstantiationException e) {
			logger.debug("实例化类：" + packageObject + "出错;" + e.getMessage());
			return null;
		} catch (IllegalAccessException e) {
			logger.debug("实例化类：" + packageObject + "出错;" + e.getMessage());
			return null;
		}
	}

	/**
	 * 获取对象属性值
	 * @param property String 属性
	 * @param object Object 对象
	 */
	public static Object getPropertyValue(String property, Object object) {
		Object fieldValue = null;
		try {
			PropertyDescriptor pd = new PropertyDescriptor(property, object.getClass());
			Method getMethod = pd.getReadMethod();// 取出当字段的get方法.
			fieldValue = getMethod.invoke(object, new Object[0]);
		} catch (IntrospectionException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e) {
			logger.debug("获取属性值：" + property + "出错;" + e.getMessage());
		} catch (IllegalAccessException e) {
			logger.debug("获取属性值：" + property + "出错;" + e.getMessage());
		} catch (InvocationTargetException e) {
			logger.debug("获取属性值：" + property + "出错;" + e.getMessage());
		}
		return fieldValue;
	}

	/**
	 * 判断对象属性字段是否是数据库中的字段
	 * @param field
	 * @return
	 * @author:chenxy
	 */
	public static boolean isDataColumnField(Field field) {
		if (field == null) {
			return false;
		}
		boolean flag = true;
		int modify = field.getModifiers();
		if (Modifier.isFinal(modify)) {
			flag = false;
		} else if (Modifier.isStatic(modify)) {
			flag = false;
		} else if (Modifier.isPublic(modify)) {
			flag = false;
		} else if (Modifier.isProtected(modify)) {
			flag = false;
		}
		if (flag) {
			if (!"java.lang.String".equals(field.getType().getName())) {
				flag = false;
			}
		}
		return flag;
	}

	/**
	 * 将对象中的属性转化成数据库中的字段(针对数据库中的字段以_做为分隔符)
	 * @param fieldName String 对象属性名称
	 * @return String 属性对应数据字段
	 * @author:chenxy
	 */
	public static String getDataColumnField(String fieldName) {
		if (StringUtils.isBlank(fieldName)) {
			return null;
		}
		StringBuffer sourceFieldName = new StringBuffer();
		for (int i = 0; i < fieldName.length(); i++) {
			if (fieldName.charAt(i) >= 65 && fieldName.charAt(i) <= 90) { // 如果是大写字母则在字符前加 "_"
				sourceFieldName.append("_");
			}
			sourceFieldName.append(fieldName.charAt(i));
		}
		return sourceFieldName.toString().toUpperCase();
	}

	/**
	 * 将数据库中的字段转化成对象中的属性(针对数据库中的字段以_做为分隔符)
	 * @param columnField String 对象属性名称
	 * @return String 属性对应数据字段
	 * @author:chenxy
	 */
	public static String getObjectPropertiesField(String columnField) {
		if (StringUtils.isBlank(columnField)) {
			return null;
		}
		String lowColumnField = columnField.toLowerCase();
		StringBuffer propertiesFieldName = new StringBuffer();
		boolean flag = false;
		for (int i = 0; i < lowColumnField.length(); i++) {
			if (lowColumnField.charAt(i) == '_') {
				flag = true;
			} else if (flag && lowColumnField.charAt(i) >= 97 && lowColumnField.charAt(i) <= 122) {
				propertiesFieldName.append((char) (lowColumnField.charAt(i) - 32));
				flag = false;
			} else {
				propertiesFieldName.append(lowColumnField.charAt(i));
				flag = false;
			}
		}
		return propertiesFieldName.toString();
	}

	/**
	 * 描述：从Request中获取对象实例
	 * @param request HttpServletRequest
	 * @param packageObject String 包.类
	 */
	public static Object makeRequestCallObject(HttpServletRequest request, String packageObject) {
		// 初始化类
		Object object = checkObject(packageObject);
		if (object != null) { // 初始化类实例
			Field[] fields = getProperties(object);
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				String fieldName = field.getName();
				Object value = request.getParameter(fieldName);
				setPropertyValue(object, fieldName, value);
			}
		}
		return object;
	}

	/**
	 * 描述：从Map中获取对象实例（数据库返回Map）
	 * @param dataMap Map 数据库JDBC查询返回Map对象
	 * @param packageObject String 包.类
	 * @return
	 * @author:chenxy
	 */
	public static Object makeMapCallObject(Map<String, Object> dataMap, String packageObject) {
		Object object = checkObject(packageObject);
		if (object != null) { // 初始化类实例
			Field[] fields = getProperties(object);
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (!isDataColumnField(field)) {
					continue;
				}
				try {
					String fieldName = field.getName();
					Object value = dataMap.get(getDataColumnField(fieldName));
					setPropertyValue(object, fieldName, value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return object;
	}

	/**
	 * 描述：从List中获取对象实例列（数据库返回List<Map>）
	 * @param requestList List 数据库JDBC查询返回List<Map>对象
	 * @param packageObject String 包.类
	 * @return List
	 * @author:chenxy
	 */
	public static List<Object> makeMapCallObjectList(List<Map<String, Object>> requestList, String packageObject) {
		if (requestList == null || requestList.size() < 2) {
			return null;
		}
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < requestList.size(); i++) {
			list.add(makeMapCallObject(requestList.get(i), packageObject));
		}
		return list;
	}

	/**
	 * 描述：从List中获取对象实例列（数据库返回List<Map>）
	 * @param requestList List 数据库JDBC查询返回List<Map>对象
	 * @param packageObject String 包.类
	 * @return Set
	 * @author:chenxy
	 */
	public static Set<Object> makeMapCallObjectSet(List<Map<String, Object>> requestList, String packageObject) {
		if (requestList == null || requestList.size() < 2) {
			return null;
		}
		Set<Object> set = new HashSet<Object>();
		for (int i = 0; i < requestList.size(); i++) {
			set.add(makeMapCallObject(requestList.get(i), packageObject));
		}
		return set;
	}

	/**
	 * 将对象转化成Map(对应数据库字段Map)
	 * @param object Object 转化对象
	 * @return Map
	 * @author:chenxy
	 */
	public static Map<String, Object> makeObjectCallMap(Object object) {
		if (object == null) {
			return null;
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Field[] fields = getProperties(object);
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String fieldName = field.getName();
			if (!isDataColumnField(field)) {
				continue;
			}
			try {
				Object value = getPropertyValue(fieldName, object);
				resultMap.put(getDataColumnField(fieldName), value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return resultMap;
	}

	/**
	 * 将orig中的数据拷贝到dest中,mapping为属性映射关系
	 * @param dest
	 * @param orig
	 * @param mapping
	 * @author:chenxy
	 */
	public static void copySimpleProperties(Object dest, Object orig, Map<String, String> mapping) {
		Field[] fields = ObjectReflectUtil.getProperties(dest);
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			int modify = field.getModifiers();
			if (Modifier.isFinal(modify) || !Modifier.isPrivate(modify) || Modifier.isStatic(modify)) {
				continue;
			}
			String fieldName = field.getName();
			String origFieldName = mapping.get(fieldName);
			if (StringUtils.isBlank(origFieldName)) {
				continue;
			}
			Object value = ObjectReflectUtil.getPropertyValue(origFieldName, orig);
			if (value == null) {
				continue;
			}
			setPropertyValue(dest, fieldName, value);
		}
	}

}