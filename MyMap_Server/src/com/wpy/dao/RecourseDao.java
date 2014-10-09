package com.wpy.dao;

import java.util.List;

import com.wpy.service.RecourseService;
import com.wpy.util.JdbcUtils;

public class RecourseDao implements RecourseService {
	private JdbcUtils jdbcUtils;

	public RecourseDao() {
		jdbcUtils = new JdbcUtils();
	}

	/**
	 * 向数据库插入数据
	 */
	@Override
	public boolean insert(List<Object> params) {
		boolean flag = false;
		try {
			String sql = "insert into recourse(posttime,usercoordinates,status) values(?,?,?)";
			jdbcUtils.getConnection();
			flag = jdbcUtils.updateByPreparedStatement(sql, params);
		} catch (Exception e) {
		} finally {
			jdbcUtils.releaseConn();
		}
		return flag;
	}

}
