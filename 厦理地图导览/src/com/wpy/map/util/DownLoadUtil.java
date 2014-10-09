package com.wpy.map.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.ProgressDialog;

/**
 * 文件下载的工具类
 * 
 * @author wpy
 * 
 */
public class DownLoadUtil {
	/**
	 * 下载文件到指定路径
	 * 
	 * @param path
	 *            文件url
	 * @param filePath
	 *            保存路径
	 * @param progressDialog
	 * @return 下载完成的文件
	 * @throws Exception
	 */
	public static File getFile(String path, String filePath,
			ProgressDialog progressDialog) throws Exception {
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();// 创建连接
		conn.setConnectTimeout(5000);// 设置超时时间
		conn.setRequestMethod("GET");
		if (conn.getResponseCode() == 200) {
			int total = conn.getContentLength();// 获取文件大小
			progressDialog.setMax(total);
			InputStream is = conn.getInputStream();// 创建输入流
			File file = new File(filePath);
			// 判断文件目录是否存在
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
