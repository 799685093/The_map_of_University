package com.wpy.map.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 完成对json数据的解析
 * 
 * @author wpy
 * 
 */
public class JsonUtil {
	private static final String BYTE = "java.lang.Byte";
	private static final String INTEGER = "java.lang.Integer";
	private static final String SHORT = "java.lang.Short";
	private static final String LONG = "java.lang.Long";
	private static final String BOOLEAN = "java.lang.Boolean";
	private static final String CHAR = "java.lang.Character";
	private static final String FLOAT = "java.lang.Float";
	private static final String DOUBLE = "java.lang.Double";

	private static final String VALUE_BYTE = "byte";
	private static final String VALUE_INTEGER = "int";
	private static final String VALUE_SHORT = "short";
	private static final String VALUE_LONG = "long";
	private static final String VALUE_BOOLEAN = "boolean";
	private static final String VALUE_CHAR = "char";
	private static final String VALUE_FLOAT = "float";
	private static final String VALUE_DOUBLE = "double";

	/**
	 * 根据key获取给定的json数据的值
	 * 
	 * @param json
	 *            给定的JSON字符串
	 * @param key
	 *            指定的要获取值所对应的key
	 * @return 返回一个字符串，表示根据指定的key所得到的值，获取失败或发生JSON解析错误则返回空字符串
	 */
	public static String getJsonValueByKey(String json, String key) {
		String value = "";
		try {
			JSONObject jo = new JSONObject(json);
			value = jo.getString(key);
		} catch (JSONException e) {
		}
		return value;
	}

	/**
	 * 将指定的JSON字符串转换成cls指定的类的实例对象
	 * 
	 * @param json
	 *            给定的JSON字符串
	 * @param cls
	 *            指定要转换成的对象所属的类型Class实例
	 * @return 返回cls指定类型的对象实例,其中的字段与json数据键值对一一对应
	 */
	public static <T> T getSimple(String json, Class<T> cls) {
		T obj = null;
		try {
			JSONObject jsonObject = new JSONObject(json);
			obj = cls.newInstance();
			Field[] fields = cls.getDeclaredFields();
			for (Field field : fields) {
				if (Modifier.isFinal(field.getModifiers())
						|| Modifier.isPrivate(field.getModifiers())) {
					continue;
				}
				try {
					String key = field.getName();
					if (jsonObject.get(key) == JSONObject.NULL) {
						field.set(obj, null);
					} else {
						Object value = getValue4Field(jsonObject.get(key),
								jsonObject.get(key).getClass().getName());
						field.set(obj, value);
					}
				} catch (JSONException e) {
					field.set(obj, null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * 将指定的JSON字符串转换成包含cls指定的类型的实体对象List集合
	 * 
	 * @param json
	 *            给定的JSON字符串
	 * @param cls
	 *            指定要转换成的对象所属的类型Class实例
	 * @return 返回一个List集合，其中包含json中的数据元素所对应的实体对象实例
	 */
	public static <T> List<T> getMoreList(String json, Class<T> cls) {
		List<T> list = new ArrayList<T>();
		try {
			JSONArray jsonArray = new JSONArray(json);
			List<String> jsonStrList = new ArrayList<String>();
			for (int i = 0; i < jsonArray.length(); i++) {
				String jsonStr = jsonArray.getString(i);
				jsonStrList.add(jsonStr);
			}
			for (String jsonStr : jsonStrList) {
				T obj = getSimple(jsonStr, cls);
				list.add(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 把指定的对象orginalValue转换成typeName指定的类型的对象
	 * 
	 * @param orginalValue
	 *            对象在转换之前的值
	 * @param fieldType
	 *            要转换的类型名称
	 * @return
	 */
	private static Object getValue4Field(Object orginalValue, String typeName) {
		Object value = orginalValue.toString();
		if (typeName.equals(BYTE) || typeName.equals(VALUE_BYTE)) {
			value = Byte.class.cast(orginalValue);
		}
		if (typeName.equals(INTEGER) || typeName.equals(VALUE_INTEGER)) {
			value = Integer.class.cast(orginalValue);
		}
		if (typeName.equals(SHORT) || typeName.equals(VALUE_SHORT)) {
			value = Short.class.cast(orginalValue);
		}
		if (typeName.equals(LONG) || typeName.equals(VALUE_LONG)) {
			value = Long.class.cast(orginalValue);
		}
		if (typeName.equals(BOOLEAN) || typeName.equals(VALUE_BOOLEAN)) {
			value = Boolean.class.cast(orginalValue);
		}
		if (typeName.equals(CHAR) || typeName.equals(VALUE_CHAR)) {
			value = Character.class.cast(orginalValue);
		}
		if (typeName.equals(FLOAT) || typeName.equals(VALUE_FLOAT)) {
			value = Float.class.cast(orginalValue);
		}
		if (typeName.equals(DOUBLE) || typeName.equals(VALUE_DOUBLE)) {
			value = Double.class.cast(orginalValue);
		}
		return value;
	}
}
