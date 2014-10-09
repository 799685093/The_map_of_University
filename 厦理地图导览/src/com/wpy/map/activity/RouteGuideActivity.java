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
 * ��������ҳ��
 * 
 * @author wpy
 * 
 */
public class RouteGuideActivity extends Activity implements OnClickListener {

	public static final String myCity = "����";

	private MyApplication app;
	private MyMapView myMapView;// ��ʾ��ͼ����ͼ
	private MapController mapController = null;// ��ͼ������
	private MyLocationOverlay myLocationOverlay = null;// ��ʾ�ҵ�λ�õĸ�����
	private LocationData locationData = null;// �ҵ�λ�õ��������
	private GeoPoint locationPoint;

	private RelativeLayout btn_ensure;
	private ImageButton navi_btn_back;
	private EditText et_end;
	private ListView list_route;

	// ��λ���
	private LocationClient mLocationClient = null;
	private BDLocationListener mBdLocationListener = new MyLocationListener();

	// �������
	private MKSearch mkSearch = null;
	private ArrayList<HashMap<String, String>> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routeguide);
		app = (MyApplication) this.getApplication();
		myMapView = (MyMapView) this.findViewById(R.id.mybmapview);
		mapController = myMapView.getController();// ��ȡ��ͼ�Ŀ���Ȩ

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

		myMapView.setBuiltInZoomControls(true);// �����������ſؼ�
		mapController.setZoom(17);// �������ż���
		mapController.enableClick(true);// ���õ��
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
		// listview�ĵ���¼�
		list_route.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				@SuppressWarnings("unchecked")
				// �õ�item�ĵ���Ϣ
				HashMap<String, String> map = (HashMap<String, String>) list_route
						.getItemAtPosition(position);
				String s = map.get("key");
				// ���ݵ�ַ���в�ѯ
				mkSearch.geocode(s, myCity);
			}
		});

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
	 * ʵ��BDLocationListener�ӿ�
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
		 * �����첽���ص�POI��ѯ�����������BDLocation���Ͳ���
		 */
		@Override
		public void onReceivePoi(BDLocation arg0) {

		}
	}

	/**
	 * ��ʼ������ģ�飬ע���¼�����
	 */
	private void initSearch() {
		mkSearch = new MKSearch();
		mkSearch.init(app.mBMapManager, new MKSearchListener() {

			// ���ز���·���������
			@Override
			public void onGetWalkingRouteResult(MKWalkingRouteResult arg0,
					int arg1) {
			}

			// ���ع����������
			@Override
			public void onGetTransitRouteResult(MKTransitRouteResult arg0,
					int arg1) {
			}

			// �����������Ϣ�������
			@Override
			public void onGetSuggestionResult(MKSuggestionResult result,
					int iError) {
				if (0 != iError) {
					Toast.makeText(RouteGuideActivity.this, "û���ҵ��õ�ַ",
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

			// ���ط���̴����
			@Override
			public void onGetShareUrlResult(MKShareUrlResult arg0, int arg1,
					int arg2) {
			}

			// ����poi�������
			@Override
			public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
			}

			// ����poi�����Ϣ�����Ľ��
			@Override
			public void onGetPoiDetailSearchResult(int arg0, int arg1) {
			}

			// ���ؼݳ�·���������
			@Override
			public void onGetDrivingRouteResult(MKDrivingRouteResult arg0,
					int arg1) {
			}

			// ���ع�����������Ϣ�������
			@Override
			public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
			}

			// ���ص�ַ��Ϣ�������
			@Override
			public void onGetAddrResult(MKAddrInfo result, int iError) {
				if (0 != iError) {
					Toast.makeText(RouteGuideActivity.this, "û���ҵ��õ�ַ",
							Toast.LENGTH_SHORT).show();
					return;
				}
				// �ж��Ƿ��ǰ���ַ��ѯ
				if (result.type == MKAddrInfo.MK_GEOCODE) {
					launchNavigator(locationPoint, result.geoPt);
				}
			}
		});
	}

	/**
	 * ʵ��TextWatcher�ӿڣ����������м���
	 * 
	 * @author wpy
	 * 
	 */
	public class watcher implements TextWatcher {
		// �����仯֮ǰ
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		// �����仯֮ʱ
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

		// �����仯֮��
		@Override
		public void afterTextChanged(Editable s) {
		}

	}

	/**
	 * ��ť�ļ����¼�
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
				Toast.makeText(this, "�������յ�λ�û��ڵ�ͼ��ѡ���յ��λ�ã���ȷ��",
						Toast.LENGTH_LONG).show();
				return;
			} else {
				// �û������յ��λ�õ�ַ ��ѯ�õ�ַ��ȡ��ַ��Ϣ
				mkSearch.geocode(et_end.getText().toString().trim(), myCity);
			}
			// ��������
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
	 * ��������յ�ľ�γ����Ϣ�������Ϣ������������
	 */
	public void launchNavigator(GeoPoint pointStart, GeoPoint pointEnd) {
		// ����ת�������ٶȵ�ͼ���õ�bd09����ת��Ϊgcj02����
		com.baidu.nplatform.comapi.basestruct.GeoPoint ptGCJStart = CoordinateTransformUtil
				.transferBD09ToGCJ02(
						(Double) (pointStart.getLongitudeE6() / 1e6),
						(Double) (pointStart.getLatitudeE6() / 1e6));
		com.baidu.nplatform.comapi.basestruct.GeoPoint ptGCJend = CoordinateTransformUtil
				.transferBD09ToGCJ02(
						(Double) (pointEnd.getLongitudeE6() / 1e6),
						(Double) (pointEnd.getLatitudeE6() / 1e6));
		// ��������
		BaiduNaviManager.getInstance().launchNavigator(
				this,
				ptGCJStart.getLatitudeE6() / 1e5,// �������1e5����Ϊgcj02��������С�����5λ�������ǵ������6λ��
				ptGCJStart.getLongitudeE6() / 1e5, "",
				ptGCJend.getLatitudeE6() / 1e5,
				ptGCJend.getLongitudeE6() / 1e5, "",
				NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME, // ��·��ʽ
				false, // true��ʵ���� falseģ�⵼��
				BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, // �����߲���
				new OnStartNavigationListener() { // ��ת����

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
	 * ��дActivity�������ڵķ����Ե�ͼ���й���
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
