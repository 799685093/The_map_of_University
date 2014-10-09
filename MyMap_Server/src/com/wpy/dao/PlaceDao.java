package com.wpy.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wpy.service.PlaceService;
import com.wpy.util.JdbcUtils;

public class PlaceDao implements PlaceService {

	private JdbcUtils jdbcUtils;

	public PlaceDao() {
		jdbcUtils = new JdbcUtils();
	}

	@Override
	public Map<String, Object> findByName(String name) {
		Map<String, Object> map = null;
		String sql = "select * from place where placename = ?";
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

	/**
	 * Ä£ºý²éÑ¯
	 */
	@Override
	public List<Map<String, Object>> getPlaceByName(String name) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql = "select * from place where placename like ?";
		List<String> params = new ArrayList<String>();
		params.add(name);
		try {
			jdbcUtils.getConnection();
			list = jdbcUtils.getMoreResult(sql, params);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			jdbcUtils.releaseConn();
		}
		return list;
	}

	@Override
	public List<Map<String, Object>> findAll() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql = "select * from place";
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

}
