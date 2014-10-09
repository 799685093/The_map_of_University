package com.wpy.service;

import java.util.List;
import java.util.Map;

public interface NewsService {
	// 查询所有信息
	public List<Map<String, Object>> getAllnews(int startNid, int count);

	// 通过id查询新闻
	public Map<String, Object> getNewsById(int id);
}
