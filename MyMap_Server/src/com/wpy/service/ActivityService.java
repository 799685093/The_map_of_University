package com.wpy.service;

import java.util.List;
import java.util.Map;

public interface ActivityService {
	// 查询所有信息
	public List<Map<String, Object>> getActivity();

	// 通过名称查询
	public Map<String, Object> findByName(String name);
}
