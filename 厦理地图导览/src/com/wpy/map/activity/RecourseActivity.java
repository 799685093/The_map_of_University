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
 * 求助页面
 * 
 * @author wpy
 * 
 */
public class RecourseActivity extends Activity implements View.OnClickListener {

	private ImageButton btn_back_recourse;
	private ImageButton btn_phone;
	private TextView phone_number;

	private final String phone = "11111111111";// 设置求助的电话号码

	private GeoPoint mylocationPoint;
	// 定位相关
	private LocationClient mLocationClient = null;
	private BDLocationListener mBdLocationListener = new MyLocationListener();

	private UpLoadAsyncTask upLoadAsyncTask;
	private String myCoordinates;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recourse);

		// 初始化按钮控件并设置监听
		btn_back_recourse = (ImageButton) this
				.findViewById(R.id.btn_back_recourse);
		btn_back_recourse.setOnClickListener(this);
		btn_phone = (ImageButton) this.findViewById(R.id.btn_phone);
		btn_phone.setOnClickListener(this);
		phone_number = (TextView) this.findViewById(R.id.phone_number);
		phone_number.setText(phone);

		mLocationClient = new LocationClient(getApplicationContext());// 声明LocationClient类
		mLocationClient.registerLocationListener(mBdLocationListener);// 注册监听函数

		setLocationOption();
		mLocationClient.start();// 开始定位
		if (mLocationClient != null && mLocationClient.isStarted()) {
			// 发起定位请求。请求过程是异步的，定位结果在监听函数onReceiveLocation中获取。
			mLocationClient.requestLocation();
		} else {
			Log.d("RouteGuideActivity", "locClient is null or not started");
		}
	}

	/**
	 * 按钮的监听事件
	 */
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back_recourse:
			this.finish();
			break;
		case R.id.btn_phone:
			new AlertDialog.Builder(this).setTitle("提示")
					.setMessage("是否确定拨打求助电话")
					.setPositiveButton("取消", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).setNegativeButton("确定", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							SimpleDateFormat formatter = new SimpleDateFormat(
									"yyyy年MM月dd日  HH:mm:ss");
							Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
							String data = formatter.format(curDate);
							Map<String, String> map = new HashMap<String, String>();
							map.put("action_flag", "add");
							map.put("posttime", data);
							map.put("usercoordinates", myCoordinates);
							map.put("status", "未阅读");
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
	 * 设置定位的相关参数
	 */
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式 高精度定位 耗电
		option.setCoorType("bd09ll");// 返回的定位结果是百度的经纬度，默认值gcj02
		option.setScanSpan(10000);// 发起定位请求的间隔时间为10000ms
		option.disableCache(true);// 禁止启用缓存定位
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		mLocationClient.setLocOption(option);
	}

	/**
	 * 实现BDLocationListener接口 进行定位
	 * 
	 * @author wpy
	 * 
	 */
	public class MyLocationListener implements BDLocationListener {

		/**
		 * 接收异步返回的定位结果，参数是BDLocation类型参数
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
		 * 接收异步返回的POI查询结果，参数是BDLocation类型参数
		 */
		@Override
		public void onReceivePoi(BDLocation arg0) {

		}
	}

	/**
	 * 异步任务上传用户求助的时间以及坐标
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
			if (result.equals("更新成功")) {
				Toast.makeText(RecourseActivity.this, "已将数据成功上传到服务器",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(RecourseActivity.this, "上传失败", Toast.LENGTH_LONG)
						.show();
			}
		}
	}
}
