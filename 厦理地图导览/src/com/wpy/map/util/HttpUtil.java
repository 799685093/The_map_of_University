package com.wpy.map.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

public class HttpUtil {

	public static final String BASE_URL = "http://192.168.43.173:8080/MyMap_Server/";
	// 创建HttpClient对象
	public static HttpClient httpClient = new DefaultHttpClient();

	public HttpUtil() {
	}

	/**
	 * 使用get方式向服务器发起请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * @return 服务器响应的字符串
	 * @throws Exception
	 */
	public static String getRequest(final String url) throws Exception {
		// FutureTask多用于耗时的计算，主线程可以在完成自己的任务后，再去获取结果。
		FutureTask<String> task = new FutureTask<String>(
				new Callable<String>() {

					@Override
					public String call() throws Exception {
						// 创建HttpGet对象
						HttpGet httpGet = new HttpGet(url);

						// 设置连接超时
						httpClient.getParams().setParameter(
								CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
						// 设置读取超时
						httpClient.getParams().setParameter(
								CoreConnectionPNames.SO_TIMEOUT, 15000);

						// 发送GET请求
						HttpResponse httpResponse = httpClient.execute(httpGet);
						// 如果服务器成功的返回响应
						if (200 == httpResponse.getStatusLine().getStatusCode()) {
							String result = EntityUtils.toString(httpResponse
									.getEntity());
							return result;
						}
						return null;
					}
				});
		new Thread(task).start();
		return task.get();
	}

	/**
	 * 使用post方式向服务器发起请求
	 * 
	 * @param url
	 *            发送请求的URl
	 * @param params
	 *            请求的参数
	 * @return 服务器的响应字符串
	 * @throws Exception
	 */
	public static String postRequest(final String url,
			final Map<String, String> rawParams) throws Exception {
		FutureTask<String> task = new FutureTask<String>(
				new Callable<String>() {

					@Override
					public String call() throws Exception {
						// 创建HttpPost对象
						HttpPost httpPost = new HttpPost(url);
						// 如果请求的参数个数比较多，可以对传递的参数进行封装
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						for (String key : rawParams.keySet()) {
							// 封装请求参数
							params.add(new BasicNameValuePair(key, rawParams
									.get(key)));
						}
						// 设置请求参数
						httpPost.setEntity(new UrlEncodedFormEntity(params,
								"utf-8"));
						// 发送post请求
						HttpResponse httpResponse = httpClient
								.execute(httpPost);
						// 如果服务器成功的返回响应
						if (200 == httpResponse.getStatusLine().getStatusCode()) {
							// 获取服务器响应字符串
							String result = EntityUtils.toString(httpResponse
									.getEntity());
							return result;
						}
						return null;
					}
				});
		new Thread(task).start();
		return task.get();
	}
}
