package com.wpy.dao;

import java.util.Map;

import com.wpy.service.UpdataInfoService;
import com.wpy.util.JdbcUtils;

public class UpdataInfoDao implements UpdataInfoService {
	private JdbcUtils jdbcUtils;

	public UpdataInfoDao() {
		jdbcUtils = new JdbcUtils();
	}

	@Override
	public Map<String, Object> getinfo() {
		Map<String, Object> map = null;
		String sql = "select * from updatainfo";
		try {
			jdbcUtils.getConnection();
			map = jdbcUtils.findSimpleResult(sql, null);
		} catch (Exception e) {
		} finally {
			jdbcUtils.releaseConn();
		}
		return map;
	}

}
