package com.wpy.util;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcUtils {

	// �������ݿ���û���������
	private final String USERNAME = "root";
	private final String PASSWORD = "mysql";
	// �������ݿ��������Ϣ
	private final String DRIVER = "com.mysql.jdbc.Driver";
	// ����������ݿ�ĵ�ַ
	private final String URL = "jdbc:mysql://localhost:3306/mymap";
	// �������ݿ������
	private Connection connection;
	// ����sql����ִ�ж���
	private PreparedStatement pstmt;
	// �����ѯ���صĽ������
	private ResultSet resultSet;

	public JdbcUtils() {
		try {
			// �����������򣬷���һ���࣬������Ҫ��JVM���Ҳ�����ָ�����࣬Ҳ����˵JVM��ִ�и���ľ�̬�����
			Class.forName(DRIVER);
			System.out.println("ע�������ɹ���");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/*
	 * ������ݿ������
	 */
	public Connection getConnection() {
		try {
			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return connection;
	}

	/**
	 * ��ɶ����ݿ�ı����ӡ�ɾ�����޸ĵĲ���
	 * 
	 * @param sql
	 *            sql���
	 * @param params
	 *            ռλ��
	 * @return
	 * @throws SQLException
	 */
	public boolean updateByPreparedStatement(String sql, List<Object> params)
			throws SQLException {
		boolean flag = false;
		int result = -1;// ��ʾ�û�ִ����ӡ�ɾ�����޸ĵ�ʱ����Ӱ������ݿ������
		pstmt = connection.prepareStatement(sql);
		int index = 1;// ��ʾռλ���ĵ�һ��λ��
		if (params != null && !params.isEmpty()) {
			for (int i = 0; i < params.size(); i++) {
				pstmt.setObject(index++, params.get(i));
			}
		}
		result = pstmt.executeUpdate();// ����Ӱ��ļ�¼������
		flag = result > 0 ? true : false;
		return flag;
	}

	/**
	 * ��ѯ���ص�����¼
	 * 
	 * @param sql
	 * @param params
	 *            ռλ��
	 * @return
	 * @throws SQLException
	 */
	public Map<String, Object> findSimpleResult(String sql, List<Object> params)
			throws SQLException {
		Map<String, Object> map = new HashMap<String, Object>();
		int index = 1;// ��ʾռλ���ĵ�һ��λ��
		pstmt = connection.prepareStatement(sql);
		if (params != null && !params.isEmpty()) {
			for (int i = 0; i < params.size(); i++) {
				pstmt.setObject(index++, params.get(i));
			}
		}
		resultSet = pstmt.executeQuery();// ���ز�ѯ�Ľ��
		// ��ȡ�� ResultSet ������еı�š����ͺ�����
		ResultSetMetaData metaData = resultSet.getMetaData();
		int col_len = metaData.getColumnCount();// ���resultSet�е�����
		while (resultSet.next()) {
			for (int i = 0; i < col_len; i++) {
				String cols_name = metaData.getColumnName(i + 1);// ��ȡ�е�����
				Object cols_value = resultSet.getObject(cols_name);// ��ȡ���е�ֵ
				if (cols_value == null) {
					cols_value = "";
				}
				map.put(cols_name, cols_value);
			}
		}
		return map;
	}

	/**
	 * ��ѯ���ض�����¼
	 * 
	 * @param sql
	 * @param params
	 *            ռλ��
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, Object>> findMoreResult(String sql,
			List<Object> params) throws SQLException {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		int index = 1;// ��ʾռλ���ĵ�һ��λ��
		pstmt = connection.prepareStatement(sql);
		if (params != null && !params.isEmpty()) {
			for (int i = 0; i < params.size(); i++) {
				pstmt.setObject(index++, params.get(i));
			}
		}
		resultSet = pstmt.executeQuery();// ���ز�ѯ�Ľ��
		// ��ȡ�� ResultSet ������еı�š����ͺ�����
		ResultSetMetaData metaData = resultSet.getMetaData();
		int cols_len = metaData.getColumnCount();// ���resultSet�е�����
		while (resultSet.next()) {
			Map<String, Object> map = new HashMap<String, Object>();
			for (int i = 0; i < cols_len; i++) {
				String cols_name = metaData.getColumnName(i + 1);
				Object cols_value = resultSet.getObject(cols_name);
				if (cols_value == null) {
					cols_value = "";
				}
				map.put(cols_name, cols_value);
			}
			list.add(map);
		}
		return list;
	}

	/**
	 * ģ����ѯ
	 * 
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, Object>> getMoreResult(String sql,
			List<String> params) throws SQLException {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		int index = 1;// ��ʾռλ���ĵ�һ��λ��
		pstmt = connection.prepareStatement(sql);
		if (params != null && !params.isEmpty()) {
			for (int i = 0; i < params.size(); i++) {
				pstmt.setString(index++, "%" + params.get(i) + "%");
			}
		}
		resultSet = pstmt.executeQuery();// ���ز�ѯ�Ľ��
		// ��ȡ�� ResultSet ������еı�š����ͺ�����
		ResultSetMetaData metaData = resultSet.getMetaData();
		int cols_len = metaData.getColumnCount();// ���resultSet�е�����
		while (resultSet.next()) {
			Map<String, Object> map = new HashMap<String, Object>();
			for (int i = 0; i < cols_len; i++) {
				String cols_name = metaData.getColumnName(i + 1);
				Object cols_value = resultSet.getObject(cols_name);
				if (cols_value == null) {
					cols_value = "";
				}
				map.put(cols_name, cols_value);
			}
			list.add(map);
		}
		return list;
	}

	/**
	 * jdbc�ķ�װ�����÷����������װ ��ѯ������¼
	 * 
	 * @param sql
	 * @param params
	 *            ռλ��
	 * @param cls
	 * @return
	 * @throws SQLException
	 */
	public <T> T findSimpleRefResult(String sql, List<Object> params,
			Class<T> cls) throws Exception {
		T resultObject = null;
		int index = 1;// ��ʾռλ���ĵ�һ��λ��
		pstmt = connection.prepareStatement(sql);
		if (params != null && !params.isEmpty()) {
			for (int i = 0; i < params.size(); i++) {
				pstmt.setObject(index++, params.get(i));
			}
		}
		resultSet = pstmt.executeQuery();
		ResultSetMetaData metaData = resultSet.getMetaData();
		int cols_len = metaData.getColumnCount();
		while (resultSet.next()) {
			// ͨ��������ƴ���ʵ��
			resultObject = cls.newInstance();
			for (int i = 0; i < cols_len; i++) {
				String cols_name = metaData.getColumnName(i + 1);
				Object cols_value = resultSet.getObject(cols_name);
				if (cols_value == null) {
					cols_value = "";
				}
				Field field = cls.getDeclaredField(cols_name);
				field.setAccessible(true);// ��javabean�ķ���privateȨ��
				field.set(resultObject, cols_value);
			}
		}
		return resultObject;
	}

	/**
	 * ͨ��������Ʒ������ݿ� ��ѯ������¼
	 * 
	 * @param <T>
	 * @param sql
	 * @param params
	 * @param cls
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> findMoreRefResult(String sql, List<Object> params,
			Class<T> cls) throws Exception {
		List<T> list = new ArrayList<T>();
		int index = 1;
		pstmt = connection.prepareStatement(sql);
		if (params != null && !params.isEmpty()) {
			for (int i = 0; i < params.size(); i++) {
				pstmt.setObject(index++, params.get(i));
			}
		}
		resultSet = pstmt.executeQuery();
		ResultSetMetaData metaData = resultSet.getMetaData();
		int cols_len = metaData.getColumnCount();
		while (resultSet.next()) {
			T resultObject = cls.newInstance();
			for (int i = 0; i < cols_len; i++) {
				String cols_name = metaData.getColumnName(i + 1);
				Object cols_value = resultSet.getObject(cols_name);
				if (cols_value == null) {
					cols_value = "";
				}
				Field field = cls.getDeclaredField(cols_name);
				field.setAccessible(true);
				field.set(resultObject, cols_value);
			}
			list.add(resultObject);
		}
		return list;
	}

	/*
	 * �ر����ݿ������
	 */
	public void releaseConn() {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
}
