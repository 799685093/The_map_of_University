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
 * ����ѶԻ���
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
	private MapController mapController = null;// ��ͼ������
	private GeoPoint mylocationPoint;
	private GeoPoint activitygeoPoint;
	// ��λ���
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
			 * ���BMapManagerû�г�ʼ�����ʼ��BMapManager
			 */
			application.mBMapManager
					.init(new MyApplication.MyGeneralListener());
		}
		setContentView(R.layout.dialog_introdetailactivity);
		activities = (Activities) getIntent()
				.getSerializableExtra("activities");
		initwidget();
		setdata();
		mapController = mapview_dialog.getController();// ��ȡ��ͼ�Ŀ���Ȩ

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
		// mapview_dialog.setBuiltInZoomControls(true);// �����������ſؼ�
		mapController.setZoom(18);// �������ż���
		mapController.enableClick(true);// ���õ��
		activitygeoPoint = ChangeGeoPoint.StringToGeoPoint(activities.location);
		initOverlay();
		mapController.setCenter(activitygeoPoint);// ���õ�ͼ�����ĵ�

		// ����ָ�����֣���Ϊ֮����MediaPlayer����
		alarmMusic = MediaPlayer.create(this, R.raw.alarm);
		alarmMusic.setLooping(true);
		// ��������
		alarmMusic.start();
	}

	/**
	 * ��ʼ���ؼ�
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
	 * ��ʾ����
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
	 * ���û�ĸ�����
	 */
	private void initOverlay() {
		ActivityOverlay activityOverlay = new ActivityOverlay(getResources()
				.getDrawable(R.drawable.icon_marka), mapview_dialog);// �����Զ���overlay
		OverlayItem item = new OverlayItem(activitygeoPoint, "", "");// ׼��overlay����
		// ����overlayͼ�꣬�粻���ã���ʹ�ô���ItemizedOverlayʱ��Ĭ��ͼ��.
		item.setMarker(getResources().getDrawable(R.drawable.icon_marka));
		activityOverlay.addItem(item);// ��item ��ӵ�overlay��
		mapview_dialog.getOverlays().add(activityOverlay);// ��overlay�����MapView��
		mapview_dialog.refresh();
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
		}

		/**
		 * �����첽���ص�POI��ѯ�����������BDLocation���Ͳ���
		 */
		@Override
		public void onReceivePoi(BDLocation arg0) {

		}
	}

	/**
	 * ����һ�����������
	 * 
	 * @author wpy
	 * 
	 */
	public class ActivityOverlay extends ItemizedOverlay<OverlayItem> {
		/**
		 * ��MapView����ItemizedOverlay
		 * 
		 * @param mark
		 *            ��ʾ�ڵ�ͼ�ϵ�ͼ��
		 * @param mapView
		 *            ��ͼ
		 */
		public ActivityOverlay(Drawable mark, MapView mapView) {
			super(mark, mapView);
		}
	}

	/**
	 * ����¼�
	 * 
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_cancle_dialog:
			// ֹͣ����
			alarmMusic.stop();
			this.finish();
			break;
		case R.id.btn_guide_dialog:
			// ֹͣ����
			alarmMusic.stop();
			launchNavigator(mylocationPoint, activitygeoPoint);
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
