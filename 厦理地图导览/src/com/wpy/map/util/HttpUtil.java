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
	// ����HttpClient����
	public static HttpClient httpClient = new DefaultHttpClient();

	public HttpUtil() {
	}

	/**
	 * ʹ��get��ʽ���������������
	 * 
	 * @param url
	 *            ���������URL
	 * @return ��������Ӧ���ַ���
	 * @throws Exception
	 */
	public static String getRequest(final String url) throws Exception {
		// FutureTask�����ں�ʱ�ļ��㣬���߳̿���������Լ����������ȥ��ȡ�����
		FutureTask<String> task = new FutureTask<String>(
				new Callable<String>() {

					@Override
					public String call() throws Exception {
						// ����HttpGet����
						HttpGet httpGet = new HttpGet(url);

						// �������ӳ�ʱ
						httpClient.getParams().setParameter(
								CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
						// ���ö�ȡ��ʱ
						httpClient.getParams().setParameter(
								CoreConnectionPNames.SO_TIMEOUT, 15000);

						// ����GET����
						HttpResponse httpResponse = httpClient.execute(httpGet);
						// ����������ɹ��ķ�����Ӧ
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
	 * ʹ��post��ʽ���������������
	 * 
	 * @param url
	 *            ���������URl
	 * @param params
	 *            ����Ĳ���
	 * @return ����������Ӧ�ַ���
	 * @throws Exception
	 */
	public static String postRequest(final String url,
			final Map<String, String> rawParams) throws Exception {
		FutureTask<String> task = new FutureTask<String>(
				new Callable<String>() {

					@Override
					public String call() throws Exception {
						// ����HttpPost����
						HttpPost httpPost = new HttpPost(url);
						// �������Ĳ��������Ƚ϶࣬���ԶԴ��ݵĲ������з�װ
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						for (String key : rawParams.keySet()) {
							// ��װ�������
							params.add(new BasicNameValuePair(key, rawParams
									.get(key)));
						}
						// �����������
						httpPost.setEntity(new UrlEncodedFormEntity(params,
								"utf-8"));
						// ����post����
						HttpResponse httpResponse = httpClient
								.execute(httpPost);
						// ����������ɹ��ķ�����Ӧ
						if (200 == httpResponse.getStatusLine().getStatusCode()) {
							// ��ȡ��������Ӧ�ַ���
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
