package com.wpy.map.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.wpy.map.R;
import com.wpy.map.util.HttpUtil;

/**
 * ����ҳ��
 * 
 * @author wpy
 * 
 */
public class RecourseActivity extends Activity implements View.OnClickListener {

	private ImageButton btn_back_recourse;
	private ImageButton btn_phone;
	private TextView phone_number;

	private final String phone = "11111111111";// ���������ĵ绰����

	private GeoPoint mylocationPoint;
	// ��λ���
	private LocationClient mLocationClient = null;
	private BDLocationListener mBdLocationListener = new MyLocationListener();

	private UpLoadAsyncTask upLoadAsyncTask;
	private String myCoordinates;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recourse);

		// ��ʼ����ť�ؼ������ü���
		btn_back_recourse = (ImageButton) this
				.findViewById(R.id.btn_back_recourse);
		btn_back_recourse.setOnClickListener(this);
		btn_phone = (ImageButton) this.findViewById(R.id.btn_phone);
		btn_phone.setOnClickListener(this);
		phone_number = (TextView) this.findViewById(R.id.phone_number);
		phone_number.setText(phone);

		mLocationClient = new LocationClient(getApplicationContext());// ����LocationClient��
		mLocationClient.registerLocationListener(mBdLocationListener);// ע���������

		setLocationOption();
		mLocationClient.start();// ��ʼ��λ
		if (mLocationClient != null && mLocationClient.isStarted()) {
			// ����λ��������������첽�ģ���λ����ڼ�������onReceiveLocation�л�ȡ��
			mLocationClient.requestLocation();
		} else {
			Log.d("RouteGuideActivity", "locClient is null or not started");
		}
	}

	/**
	 * ��ť�ļ����¼�
	 */
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back_recourse:
			this.finish();
			break;
		case R.id.btn_phone:
			new AlertDialog.Builder(this).setTitle("��ʾ")
					.setMessage("�Ƿ�ȷ�����������绰")
					.setPositiveButton("ȡ��", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).setNegativeButton("ȷ��", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							SimpleDateFormat formatter = new SimpleDateFormat(
									"yyyy��MM��dd��  HH:mm:ss");
							Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
							String data = formatter.format(curDate);
							Map<String, String> map = new HashMap<String, String>();
							map.put("action_flag", "add");
							map.put("posttime", data);
							map.put("usercoordinates", myCoordinates);
							map.put("status", "δ�Ķ�");
							String url = HttpUtil.BASE_URL
									+ "servlet/RecourseServlet";
							upLoadAsyncTask = new UpLoadAsyncTask();
							upLoadAsyncTask.execute(map, url);
							Intent phoneIntent = new Intent(
									"android.intent.action.CALL", Uri
											.parse("tel:" + phone));
							startActivity(phoneIntent);
							dialog.dismiss();
						}
					}).create().show();
			break;
		}
	}

	/**
	 * ���ö�λ����ز���
	 */
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// ���ö�λģʽ �߾��ȶ�λ �ĵ�
		option.setCoorType("bd09ll");// ���صĶ�λ����ǰٶȵľ�γ�ȣ�Ĭ��ֵgcj02
		option.setScanSpan(10000);// ����λ����ļ��ʱ��Ϊ10000ms
		option.disableCache(true);// ��ֹ���û��涨λ
		option.setIsNeedAddress(true);// ���صĶ�λ���������ַ��Ϣ
		mLocationClient.setLocOption(option);
	}

	/**
	 * ʵ��BDLocationListener�ӿ� ���ж�λ
	 * 
	 * @author wpy
	 * 
	 */
	public class MyLocationListener implements BDLocationListener {

		/**
		 * �����첽���صĶ�λ�����������BDLocation���Ͳ���
		 */
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}
			mylocationPoint = new GeoPoint(
					(int) (location.getLatitude() * 1e6),
					(int) (location.getLongitude() * 1e6));
			myCoordinates = String.valueOf(mylocationPoint.getLongitudeE6())
					+ "," + String.valueOf(mylocationPoint.getLatitudeE6());
		}

		/**
		 * �����첽���ص�POI��ѯ�����������BDLocation���Ͳ���
		 */
		@Override
		public void onReceivePoi(BDLocation arg0) {

		}
	}

	/**
	 * �첽�����ϴ��û�������ʱ���Լ�����
	 * 
	 * @author wpy
	 * 
	 */
	private class UpLoadAsyncTask extends AsyncTask<Object, Integer, String> {

		@SuppressWarnings("unchecked")
		@Override
		protected String doInBackground(Object... params) {
			Map<String, String> map = (Map<String, String>) params[0];
			String url = (String) params[1];
			String result = null;
			try {
				result = HttpUtil.postRequest(url, map);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result.equals("���³ɹ�")) {
				Toast.makeText(RecourseActivity.this, "�ѽ����ݳɹ��ϴ���������",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(RecourseActivity.this, "�ϴ�ʧ��", Toast.LENGTH_LONG)
						.show();
			}
		}
	}
}
