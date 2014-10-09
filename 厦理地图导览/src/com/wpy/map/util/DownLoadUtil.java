package com.wpy.map.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.ProgressDialog;

/**
 * �ļ����صĹ�����
 * 
 * @author wpy
 * 
 */
public class DownLoadUtil {
	/**
	 * �����ļ���ָ��·��
	 * 
	 * @param path
	 *            �ļ�url
	 * @param filePath
	 *            ����·��
	 * @param progressDialog
	 * @return ������ɵ��ļ�
	 * @throws Exception
	 */
	public static File getFile(String path, String filePath,
			ProgressDialog progressDialog) throws Exception {
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();// ��������
		conn.setConnectTimeout(5000);// ���ó�ʱʱ��
		conn.setRequestMethod("GET");
		if (conn.getResponseCode() == 200) {
			int total = conn.getContentLength();// ��ȡ�ļ���С
			progressDialog.setMax(total);
			InputStream is = conn.getInputStream();// ����������
			File file = new File(filePath);
			// �ж��ļ�Ŀ¼�Ƿ����
			if (!file.exists()) {
				file.mkdir();
			}
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int len = 0;
			int progress = 0;
			while ((len = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
				progress += len;
				progressDialog.setProgress(progress);
			}
			fos.flush();
			fos.close();
			is.close();
			return file;
		} else
			return null;

	}
}
