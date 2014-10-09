package com.wpy.map.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionInfo;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.baidu.navisdk.util.common.CoordinateTransformUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.wpy.map.R;
import com.wpy.map.common.MyApplication;
import com.wpy.map.overlay.GuideOverlay;
import com.wpy.map.view.MyMapView;

/**
 * 导航输入页面
 * 
 * @author wpy
 * 
 */
public class RouteGuideActivity extends Activity implements OnClickListener {

	public static final String myCity = "厦门";

	private MyApplication app;
	private MyMapView myMapView;// 显示地图的视图
	private MapController mapController = null;// 地图控制器
	private MyLocationOverlay myLocationOverlay = null;// 显示我的位置的覆盖物
	private LocationData locationData = null;// 我的位置的相关数据
	private GeoPoint locationPoint;

	private RelativeLayout btn_ensure;
	private ImageButton navi_btn_back;
	private EditText et_end;
	private ListView list_route;

	// 定位相关
	private LocationClient mLocationClient = null;
	private BDLocationListener mBdLocationListener = new MyLocationListener();

	// 搜索相关
	private MKSearch mkSearch = null;
	private ArrayList<HashMap<String, String>> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routeguide);
		app = (MyApplication) this.getApplication();
		myMapView = (MyMapView) this.findViewById(R.id.mybmapview);
		mapController = myMapView.getController();// 获取地图的控制权

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

		myMapView.setBuiltInZoomControls(true);// 启用内置缩放控件
		mapController.setZoom(17);// 设置缩放级别
		mapController.enableClick(true);// 设置点击
		myLocationOverlay = new MyLocationOverlay(myMapView);
		locationData = new LocationData();
		myLocationOverlay.setData(locationData);
		myMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		myMapView.refresh();

		initSearch();

		list = new ArrayList<HashMap<String, String>>();

		btn_ensure = (RelativeLayout) this.findViewById(R.id.btn_ensure);
		btn_ensure.setOnClickListener(this);
		navi_btn_back = (ImageButton) this.findViewById(R.id.navi_btn_back);
		navi_btn_back.setOnClickListener(this);
		et_end = (EditText) this.findViewById(R.id.et_end);
		et_end.addTextChangedListener(new watcher());
		list_route = (ListView) this.findViewById(R.id.list_route);
		// listview的点击事件
		list_route.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				@SuppressWarnings("unchecked")
				// 得到item的的信息
				HashMap<String, String> map = (HashMap<String, String>) list_route
						.getItemAtPosition(position);
				String s = map.get("key");
				// 根据地址进行查询
				mkSearch.geocode(s, myCity);
			}
		});

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
	 * 实现BDLocationListener接口
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
			locationData.latitude = location.getLatitude();
			locationData.longitude = location.getLongitude();
			locationData.accuracy = location.getRadius();
			locationData.direction = location.getDerect();
			myLocationOverlay.setData(locationData);
			myMapView.refresh();
			locationPoint = new GeoPoint((int) (locationData.latitude * 1e6),
					(int) (locationData.longitude * 1e6));
			myMapView.setlocationPoint(locationPoint);
			mapController.animateTo(locationPoint);
		}

		/**
		 * 接收异步返回的POI查询结果，参数是BDLocation类型参数
		 */
		@Override
		public void onReceivePoi(BDLocation arg0) {

		}
	}

	/**
	 * 初始化搜索模块，注册事件监听
	 */
	private void initSearch() {
		mkSearch = new MKSearch();
		mkSearch.init(app.mBMapManager, new MKSearchListener() {

			// 返回步行路线搜索结果
			@Override
			public void onGetWalkingRouteResult(MKWalkingRouteResult arg0,
					int arg1) {
			}

			// 返回公交搜索结果
			@Override
			public void onGetTransitRouteResult(MKTransitRouteResult arg0,
					int arg1) {
			}

			// 返回联想词信息搜索结果
			@Override
			public void onGetSuggestionResult(MKSuggestionResult result,
					int iError) {
				if (0 != iError) {
					Toast.makeText(RouteGuideActivity.this, "没有找到该地址",
							Toast.LENGTH_SHORT).show();
					return;
				}
				list.clear();
				HashMap<String, String> hashMap = new HashMap<String, String>();
				for (int i = 0; i < result.getSuggestionNum(); i++) {
					MKSuggestionInfo info = result.getSuggestion(i);
					hashMap.put("city", info.city);
					hashMap.put("district", info.district);
					hashMap.put("key", info.key);
					if (i == 0) {
						list.add(hashMap);
					} else if (i > 0
							&& info.equals(result.getSuggestion(i - 1))) {

					}

				}
				SimpleAdapter adapter = new SimpleAdapter(
						RouteGuideActivity.this, list, R.layout.item_list_city,
						new String[] { "key", "district" }, new int[] {
								R.id.key, R.id.district });
				list_route.setAdapter(adapter);
				Log.d(">>>666666666666666>>>", "66666666");

			}

			// 返回分享短串结果
			@Override
			public void onGetShareUrlResult(MKShareUrlResult arg0, int arg1,
					int arg2) {
			}

			// 返回poi搜索结果
			@Override
			public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
			}

			// 返回poi相关信息搜索的结果
			@Override
			public void onGetPoiDetailSearchResult(int arg0, int arg1) {
			}

			// 返回驾乘路线搜索结果
			@Override
			public void onGetDrivingRouteResult(MKDrivingRouteResult arg0,
					int arg1) {
			}

			// 返回公交车详情信息搜索结果
			@Override
			public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
			}

			// 返回地址信息搜索结果
			@Override
			public void onGetAddrResult(MKAddrInfo result, int iError) {
				if (0 != iError) {
					Toast.makeText(RouteGuideActivity.this, "没有找到该地址",
							Toast.LENGTH_SHORT).show();
					return;
				}
				// 判断是否是按地址查询
				if (result.type == MKAddrInfo.MK_GEOCODE) {
					launchNavigator(locationPoint, result.geoPt);
				}
			}
		});
	}

	/**
	 * 实现TextWatcher接口，对输入框进行监听
	 * 
	 * @author wpy
	 * 
	 */
	public class watcher implements TextWatcher {
		// 输入框变化之前
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		// 输入框变化之时
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (TextUtils.isEmpty(s) && 0 == count && 0 == start) {
				myMapView.setVisibility(View.VISIBLE);
				list_route.setVisibility(View.GONE);
			} else {
				mkSearch.suggestionSearch(s.toString(), myCity);
				list_route.setVisibility(View.VISIBLE);
				myMapView.setVisibility(View.GONE);
			}
		}

		// 输入框变化之后
		@Override
		public void afterTextChanged(Editable s) {
		}

	}

	/**
	 * 按钮的监听事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_ensure:
			GeoPoint pointStart = null;
			GeoPoint pointEnd = null;
			if (GuideOverlay.tapOverlay
					&& et_end.getText().toString().trim().isEmpty()) {
				pointStart = GuideOverlay.getpointStart();
				pointEnd = GuideOverlay.getpointEnd();
			} else if (et_end.getText().toString().trim().isEmpty()) {
				Toast.makeText(this, "请输入终点位置或在地图上选择终点的位置，并确定",
						Toast.LENGTH_LONG).show();
				return;
			} else {
				// 用户输入终点的位置地址 查询该地址获取地址信息
				mkSearch.geocode(et_end.getText().toString().trim(), myCity);
			}
			// 测试数据
			// pointStart = new GeoPoint(24629695, 118099836);
			// pointEnd = new GeoPoint(24630459, 118099255);
			launchNavigator(pointStart, pointEnd);
			break;
		case R.id.navi_btn_back:
			this.finish();
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
						Intent intent = new Intent(RouteGuideActivity.this,
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
		mkSearch.destory();
		myMapView.destroy();
		mLocationClient.stop();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		myMapView.onPause();
		mLocationClient.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		myMapView.onResume();
		mLocationClient.start();
		super.onResume();
	}
}
