package com.wpy.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wpy.service.NewsService;
import com.wpy.util.JdbcUtils;

public class NewsDao implements NewsService {
	private JdbcUtils jdbcUtils;

	public NewsDao() {
		jdbcUtils = new JdbcUtils();
	}

	@Override
	public List<Map<String, Object>> getAllnews(int startNid, int count) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql = "select * from news ORDER BY time desc LIMIT ?,?";
		List<Object> params = new ArrayList<Object>();
		params.add(startNid);
		params.add(count);
		try {
			jdbcUtils.getConnection();
			list = jdbcUtils.findMoreResult(sql, params);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			jdbcUtils.releaseConn();
		}
		return list;
	}

	@Override
	public Map<String, Object> getNewsById(int id) {
		Map<String, Object> map = null;
		try {
			String sql = "select * from news where id = ?";
			List<Object> params = new ArrayList<Object>();
			params.add(id);
			jdbcUtils.getConnection();
			map = jdbcUtils.findSimpleResult(sql, params);
		} catch (Exception e) {
		} finally {
			jdbcUtils.releaseConn();
		}
		return map;
	}

}
