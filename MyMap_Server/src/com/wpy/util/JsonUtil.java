package com.wpy.util;

import net.sf.json.JSONObject;

public class JsonUtil {

	public JsonUtil() {
	}

	/**
	 * 
	 * @param key
	 *            ��ʾjson�ַ�����ͷ��Ϣ
	 * @param value
	 *            �ǶԽ������ϵ�����
	 * @return
	 */
	public static String createJsonString(String key, Object value) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(key, value);
		return jsonObject.toString();
	}
}
