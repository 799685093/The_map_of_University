package com.wpy.dao;

import java.util.List;

import com.wpy.service.FeedbackService;
import com.wpy.util.JdbcUtils;

public class FeedbackDao implements FeedbackService {
	private JdbcUtils jdbcUtils;

	public FeedbackDao() {
		jdbcUtils = new JdbcUtils();
	}

	/**
	 * 向数据库插入数据
	 */
	@Override
	public boolean insert(List<Object> params) {
		boolean flag = false;
		try {
			String sql = "insert into feedback(content,contact,status) values(?,?,?)";
			jdbcUtils.getConnection();
			flag = jdbcUtils.updateByPreparedStatement(sql, params);
		} catch (Exception e) {
		} finally {
			jdbcUtils.releaseConn();
		}
		return flag;
	}

}
