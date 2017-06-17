package com.whz.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Map;

public class ObjectPropertiesUtil {

	private static final String TRUE_STR = "1";
	private static final String TRUE = "true";
	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ObjectPropertiesUtil.class);

	public static void copySimplePropertiesValue(Field field, Object value, Object dest) {
		Class<?> fieldType = field.getType();
		String fildTypeName = fieldType.getName();
		try {
			if (fieldType.equals(String.class)) {
				field.set(dest, value);
			} else if (fieldType.equals(Date.class)) {
				Date date = DateUtil.parse((String) value);
				field.set(dest, date);
			} else if (fieldType.equals(Integer.class) || fildTypeName.equals("int")) {
				int in = Integer.parseInt((String) value);
				field.setInt(dest, in);
			} else if (fieldType.equals(Long.class) || fildTypeName.equals("long")) {
				long l = Long.parseLong((String) value);
				field.setLong(dest, l);
			} else if (fieldType.equals(Short.class) || fildTypeName.equals("short")) {
				short s = Short.parseShort((String) value);
				field.setShort(dest, s);
			} else if (fieldType.equals(Byte.class) || fildTypeName.equals("byte")) {
				byte b = Byte.parseByte((String) value);
				field.setByte(dest, b);
			} else if (fieldType.equals(Boolean.class) || fildTypeName.equals("boolean")) {
				boolean z = false;
				if (TRUE_STR.equals(value) || TRUE.equalsIgnoreCase((String) value)) {
					z = true;
				} else {
					z = false;
				}
				field.setBoolean(dest, z);
			} else if (fieldType.equals(Float.class) || fildTypeName.equals("float")) {
				float f = Float.parseFloat((String) value);
				field.setFloat(dest, f);
			} else if (fieldType.equals(Double.class) || fildTypeName.equals("double")) {
				double d = Double.parseDouble((String) value);
				field.setDouble(dest, d);
			} else if (fieldType.equals(Character.class) || fildTypeName.equals("char")) {
				char c = ((String) value).toCharArray()[0];
				field.setChar(dest, c);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	// 给dest属性赋值
	public static void copyPropertiesValue(Field field, Object value, Object dest) {
		Class<?> fieldType = field.getType();
		try {
			if (value instanceof String) {
				copySimplePropertiesValue(field, value, dest);
			} else if (value.getClass().equals(fieldType)) {
				field.set(dest, value);
			} else if (value.getClass().getName().indexOf("java.lang.Object") != -1) {
				field.set(dest, value);
			} else {
				logger.debug("不支持的属性类型：" + fieldType);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	// 将orig中的数据拷贝到dest中的静态属性
	public static void copyStaticMustProperties(Object dest, Map<String, ?> orig) throws Exception {
		Field[] fields = ObjectReflectUtil.getProperties(dest);
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			int modify = field.getModifiers();
			if (Modifier.isFinal(modify) || (!Modifier.isPublic(modify) && !Modifier.isStatic(modify))) {
				continue;
			}
			String fieldName = field.getName();
			Object value = orig.get(fieldName);
			if (value == null) {
//				logger.error("配置信息项[" + fieldName + "]读取失败,检查参数表数据是否已经配置完成...");
			} else {
				copyPropertiesValue(field, value, dest);
			}
		}
	}

	 // 将orig中的数据拷贝到dest中的静态属性
	public static void copyStaticProperties(Object dest, Map<String, ?> orig) {
		Field[] fields = ObjectReflectUtil.getProperties(dest);
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			int modify = field.getModifiers();
			if (Modifier.isFinal(modify) || (!Modifier.isPublic(modify) && !Modifier.isStatic(modify))) {
				continue;
			}
			String fieldName = field.getName();
			Object value = orig.get(fieldName);
			if (value == null) {
				continue;
			}
			copyPropertiesValue(field, value, dest);
		}
	}

}