package com.wpy.service;

import java.util.List;
import java.util.Map;

public interface ActivityService {
	// ��ѯ������Ϣ
	public List<Map<String, Object>> getActivity();

	// ͨ�����Ʋ�ѯ
	public Map<String, Object> findByName(String name);
}
