package com.wpy.service;

import java.util.List;
import java.util.Map;

public interface PlaceService {

	//ͨ�����Ʋ�ѯ
	public Map<String, Object> findByName(String name);
	public List<Map<String, Object>> getPlaceByName(String name);
	//��ѯ������Ϣ
	public List<Map<String, Object>> findAll();
}
