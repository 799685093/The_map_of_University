package com.wpy.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wpy.service.ActivityService;
import com.wpy.util.JdbcUtils;

public class ActivityDao implements ActivityService {

	private JdbcUtils jdbcUtils;

	public ActivityDao() {
		jdbcUtils = new JdbcUtils();
	}

	@Override
	public List<Map<String, Object>> getActivity() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql = "select * from activities where status = 'true'";
		try {
			jdbcUtils.getConnection();
			list = jdbcUtils.findMoreResult(sql, null);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			jdbcUtils.releaseConn();
		}
		return list;
	}

	@Override
	public Map<String, Object> findByName(String name) {
		Map<String, Object> map = null;
		String sql = "select * from activities where name = ?";
		List<Object> params = new ArrayList<Object>();
		params.add(name);
		try {
			jdbcUtils.getConnection();
			map = jdbcUtils.findSimpleResult(sql, params);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			jdbcUtils.releaseConn();
		}
		return map;
	}

}
