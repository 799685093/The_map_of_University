package com.wpy.map.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.baidu.navisdk.util.common.CoordinateTransformUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.wpy.map.R;
import com.wpy.map.common.ChangeGeoPoint;
import com.wpy.map.common.MyApplication;
import com.wpy.map.entity.Activities;

/**
 * 活动提醒对话框
 * 
 * @author wpy
 * 
 */
public class EventRemindDialogActivity extends Activity implements
		OnClickListener {

	private TextView tv_activity_title_dialog;
	private TextView tv_Organizers_dialog;
	private TextView tv_activity_time_dialog;
	private TextView tv_address_dialog;
	private TextView tv_activity_details_dialog;
	private MapView mapview_dialog;
	private RelativeLayout btn_cancle_dialog;
	private RelativeLayout btn_guide_dialog;

	private Activities activities;
	private MapController mapController = null;// 地图控制器
	private GeoPoint mylocationPoint;
	private GeoPoint activitygeoPoint;
	// 定位相关
	private LocationClient mLocationClient = null;
	private BDLocationListener mBdLocationListener = new MyLocationListener();

	MediaPlayer alarmMusic;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication application = (MyApplication) this.getApplication();
		if (application.mBMapManager == null) {
			application.mBMapManager = new BMapManager(getApplicationContext());
			/**
			 * 如果BMapManager没有初始化则初始化BMapManager
			 */
			application.mBMapManager
					.init(new MyApplication.MyGeneralListener());
		}
		setContentView(R.layout.dialog_introdetailactivity);
		activities = (Activities) getIntent()
				.getSerializableExtra("activities");
		initwidget();
		setdata();
		mapController = mapview_dialog.getController();// 获取地图的控制权

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
		// mapview_dialog.setBuiltInZoomControls(true);// 启用内置缩放控件
		mapController.setZoom(18);// 设置缩放级别
		mapController.enableClick(true);// 设置点击
		activitygeoPoint = ChangeGeoPoint.StringToGeoPoint(activities.location);
		initOverlay();
		mapController.setCenter(activitygeoPoint);// 设置地图的中心点

		// 加载指定音乐，并为之创建MediaPlayer对象
		alarmMusic = MediaPlayer.create(this, R.raw.alarm);
		alarmMusic.setLooping(true);
		// 播放音乐
		alarmMusic.start();
	}

	/**
	 * 初始化控件
	 */
	private void initwidget() {
		tv_activity_title_dialog = (TextView) this
				.findViewById(R.id.tv_activity_title_dialog);
		tv_Organizers_dialog = (TextView) this
				.findViewById(R.id.tv_Organizers_dialog);
		tv_activity_time_dialog = (TextView) this
				.findViewById(R.id.tv_activity_time_dialog);
		tv_address_dialog = (TextView) this
				.findViewById(R.id.tv_address_dialog);
		tv_activity_details_dialog = (TextView) this
				.findViewById(R.id.tv_activity_details_dialog);
		mapview_dialog = (MapView) this.findViewById(R.id.mapview_dialog);
		btn_cancle_dialog = (RelativeLayout) this
				.findViewById(R.id.btn_cancle_dialog);
		btn_cancle_dialog.setOnClickListener(this);
		btn_guide_dialog = (RelativeLayout) this
				.findViewById(R.id.btn_guide_dialog);
		btn_guide_dialog.setOnClickListener(this);
	}

	/**
	 * 显示数据
	 */
	private void setdata() {
		tv_activity_title_dialog.setText(activities.name);
		tv_Organizers_dialog.setText(activities.organizer);
		tv_activity_time_dialog.setText(activities.begintime + " - "
				+ activities.endtime);
		tv_address_dialog.setText(activities.address);
		tv_activity_details_dialog.setText(activities.intro);
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
	 * 设置活动的覆盖物
	 */
	private void initOverlay() {
		ActivityOverlay activityOverlay = new ActivityOverlay(getResources()
				.getDrawable(R.drawable.icon_marka), mapview_dialog);// 创建自定义overlay
		OverlayItem item = new OverlayItem(activitygeoPoint, "", "");// 准备overlay数据
		// 设置overlay图标，如不设置，则使用创建ItemizedOverlay时的默认图标.
		item.setMarker(getResources().getDrawable(R.drawable.icon_marka));
		activityOverlay.addItem(item);// 将item 添加到overlay中
		mapview_dialog.getOverlays().add(activityOverlay);// 将overlay添加至MapView中
		mapview_dialog.refresh();
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
		}

		/**
		 * 接收异步返回的POI查询结果，参数是BDLocation类型参数
		 */
		@Override
		public void onReceivePoi(BDLocation arg0) {

		}
	}

	/**
	 * 定义一个覆盖物的类
	 * 
	 * @author wpy
	 * 
	 */
	public class ActivityOverlay extends ItemizedOverlay<OverlayItem> {
		/**
		 * 用MapView构造ItemizedOverlay
		 * 
		 * @param mark
		 *            显示在地图上的图标
		 * @param mapView
		 *            视图
		 */
		public ActivityOverlay(Drawable mark, MapView mapView) {
			super(mark, mapView);
		}
	}

	/**
	 * 点击事件
	 * 
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_cancle_dialog:
			// 停止音乐
			alarmMusic.stop();
			this.finish();
			break;
		case R.id.btn_guide_dialog:
			// 停止音乐
			alarmMusic.stop();
			launchNavigator(mylocationPoint, activitygeoPoint);
			break;
		}
	}

	/**
	 * 将起点与终点的经纬度信息等相关信息传给导航界面
	 */
	public void launchNavigator(GeoPoint pointStart, GeoPoint pointEnd) {
		// 坐标转换，将百度地图所用的bd09坐标转换为gcj02坐标
		com.baidu.nplatform.comapi.basestruct.GeoPoint ptGCJStart = CoordinateTransformUtil
				.transferBD09ToGCJ02(
						(Double) (pointStart.getLongitudeE6() / 1e6),
						(Double) (pointStart.getLatitudeE6() / 1e6));
		com.baidu.nplatform.comapi.basestruct.GeoPoint ptGCJend = CoordinateTransformUtil
				.transferBD09ToGCJ02(
						(Double) (pointEnd.getLongitudeE6() / 1e6),
						(Double) (pointEnd.getLatitudeE6() / 1e6));
		// 启动导航
		BaiduNaviManager.getInstance().launchNavigator(
				this,
				ptGCJStart.getLatitudeE6() / 1e5,// 必须除以1e5，因为gcj02的坐标是小数点后5位，而我们的坐标的6位的
				ptGCJStart.getLongitudeE6() / 1e5, "",
				ptGCJend.getLatitudeE6() / 1e5,
				ptGCJend.getLongitudeE6() / 1e5, "",
				NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME, // 算路方式
				false, // true真实导航 false模拟导航
				BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, // 在离线策略
				new OnStartNavigationListener() { // 跳转监听

					@Override
					public void onJumpToNavigator(Bundle configParams) {
						Intent intent = new Intent(
								EventRemindDialogActivity.this,
								BNavigatorActivity.class);
						intent.putExtras(configParams);
						startActivity(intent);
					}

					@Override
					public void onJumpToDownloader() {
					}
				});
	}

	/**
	 * 重写Activity生命周期的方法对地图进行管理
	 */
	@Override
	protected void onStart() {
		mLocationClient.start();
		super.onStart();
	}

	@Override
	protected void onStop() {
		mLocationClient.stop();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mapview_dialog.destroy();
		mLocationClient.stop();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mapview_dialog.onPause();
		mLocationClient.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mapview_dialog.onResume();
		mLocationClient.start();
		super.onResume();
	}
}
