package com.xiyuan.args;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xiyuan_fengyu on 2016/8/26.
 */
public class CastUtil {

	private static Map<Class<?>, ClassAndMethod> classNameAndMethod = new HashMap<>();

	private static class ClassAndMethod {
		private Class<?> clazz;
		private Method method;

		public ClassAndMethod(Class<?> clazz, Method method) {
			this.clazz = clazz;
			this.method = method;
		}
	}

	static {
		try {
			{
				Method method = Byte.class.getMethod("valueOf", String.class);
				ClassAndMethod classAndMethod = new ClassAndMethod(Byte.class, method);
				classNameAndMethod.put(Byte.class, classAndMethod);
				classNameAndMethod.put(byte.class, classAndMethod);
			}

			{
				Method method = Boolean.class.getMethod("valueOf", String.class);
				ClassAndMethod classAndMethod = new ClassAndMethod(Boolean.class, method);
				classNameAndMethod.put(Boolean.class, classAndMethod);
				classNameAndMethod.put(boolean.class, classAndMethod);
			}

			{
				Method method = Short.class.getMethod("valueOf", String.class);
				ClassAndMethod classAndMethod = new ClassAndMethod(Short.class, method);
				classNameAndMethod.put(Short.class, classAndMethod);
				classNameAndMethod.put(short.class, classAndMethod);
			}

			{
				Method method = Integer.class.getMethod("valueOf", String.class);
				ClassAndMethod classAndMethod = new ClassAndMethod(Integer.class, method);
				classNameAndMethod.put(Integer.class, classAndMethod);
				classNameAndMethod.put(int.class, classAndMethod);
			}

			{
				Method method = Long.class.getMethod("valueOf", String.class);
				ClassAndMethod classAndMethod = new ClassAndMethod(Long.class, method);
				classNameAndMethod.put(Long.class, classAndMethod);
				classNameAndMethod.put(long.class, classAndMethod);
			}

			{
				Method method = Float.class.getMethod("valueOf", String.class);
				ClassAndMethod classAndMethod = new ClassAndMethod(Float.class, method);
				classNameAndMethod.put(Float.class, classAndMethod);
				classNameAndMethod.put(float.class, classAndMethod);
			}

			{
				Method method = Double.class.getMethod("valueOf", String.class);
				ClassAndMethod classAndMethod = new ClassAndMethod(Double.class, method);
				classNameAndMethod.put(Double.class, classAndMethod);
				classNameAndMethod.put(double.class, classAndMethod);
			}

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	private static Pattern datePattern = Pattern.compile("(\\d{4,4})?[-_\\/]?(\\d{1,2})?[-_\\/]?(\\d{1,2})?[ ,_]?(\\d{1,2})?[:]?(\\d{1,2})?[:]?(\\d{1,2})?");

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static Object cast(String strVal, Class clazz) {
		if (strVal == null || clazz == null) {
			return null;
		}

		try {
			if (clazz == String.class) {
				return strVal;
			}
			else if (clazz == char.class || clazz == Character.class) {
				return strVal.isEmpty() ? '\0' : strVal.charAt(0);
			}
			else if (clazz == Date.class) {
				if (strVal.matches("\\d{13,13}")) {
					return new Date(Long.parseLong(strVal));
				}
				else {
					return parseDate(strVal);
				}
			}
			else {
				ClassAndMethod classAndMethod = classNameAndMethod.get(clazz);
				if (classAndMethod != null) {
					return classAndMethod.method.invoke(classAndMethod.clazz, strVal);
				}
			}

		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return strVal;
	}

	private static Date parseDate(String value) {
		Matcher dateMatcher = datePattern.matcher(value);
		if (dateMatcher.find()) {
			String dateStr = "";
			String groupItem = dateMatcher.group(1);
			if (groupItem == null) {
				dateStr += "1997";
			}
			else if (groupItem.length() == 1) {
				dateStr += "0" + groupItem;
			}
			else {
				dateStr += groupItem;
			}
			dateStr += "-";

			groupItem = dateMatcher.group(2);
			if (groupItem == null) {
				dateStr += "01";
			}
			else if (groupItem.length() == 1) {
				dateStr += "0" + groupItem;
			}
			else {
				dateStr += groupItem;
			}
			dateStr += "-";

			groupItem = dateMatcher.group(3);
			if (groupItem == null) {
				dateStr += "01";
			}
			else if (groupItem.length() == 1) {
				dateStr += "0" + groupItem;
			}
			else {
				dateStr += groupItem;
			}
			dateStr += " ";

			groupItem = dateMatcher.group(4);
			if (groupItem == null) {
				dateStr += "00";
			}
			else if (groupItem.length() == 1) {
				dateStr += "0" + groupItem;
			}
			else {
				dateStr += groupItem;
			}
			dateStr += ":";

			groupItem = dateMatcher.group(5);
			if (groupItem == null) {
				dateStr += "00";
			}
			else if (groupItem.length() == 1) {
				dateStr += "0" + groupItem;
			}
			else {
				dateStr += groupItem;
			}
			dateStr += ":";

			groupItem = dateMatcher.group(6);
			if (groupItem == null) {
				dateStr += "00";
			}
			else if (groupItem.length() == 1) {
				dateStr += "0" + groupItem;
			}
			else {
				dateStr += groupItem;
			}

			try {
				return dateFormat.parse(dateStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
