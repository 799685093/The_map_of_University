package com.wpy.util;

import net.sf.json.JSONObject;

public class JsonUtil {

	public JsonUtil() {
	}

	/**
	 * 
	 * @param key
	 *            表示json字符串的头信息
	 * @param value
	 *            是对解析集合的类型
	 * @return
	 */
	public static String createJsonString(String key, Object value) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(key, value);
		return jsonObject.toString();
	}
}
