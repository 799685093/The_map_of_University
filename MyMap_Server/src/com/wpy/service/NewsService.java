package com.wpy.service;

import java.util.List;
import java.util.Map;

public interface NewsService {
	// ��ѯ������Ϣ
	public List<Map<String, Object>> getAllnews(int startNid, int count);

	// ͨ��id��ѯ����
	public Map<String, Object> getNewsById(int id);
}
