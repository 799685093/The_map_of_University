package com.wpy.map.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.wpy.map.R;
import com.wpy.map.common.ChangeGeoPoint;
import com.wpy.map.entity.Activities;
import com.wpy.map.overlay.ActivityOverlay;
import com.wpy.map.util.HttpUtil;
import com.wpy.map.util.JsonUtil;

/**
 * У԰���ͼҳ��
 * 
 * @author wpy
 * 
 */
public class IntroActivityMapActivity extends Activity implements
		OnClickListener {

	private MapView mMapView;// ��ʾ��ͼ����ͼ
	private ImageButton btn_back_introactivity;
	// �������ľ�γ��118.093717,24.629962
	public double lon = 118.093717;// ����
	public double lat = 24.629962;// γ��

	private Drawable mark;// ��ע��
	private ActivityOverlay itemOverlay;
	private LoadActivityAsyncTask loadActivityAsyncTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_introactivity);
		btn_back_introactivity = (ImageButton) this
				.findViewById(R.id.btn_back_introactivity);// ��ʼ���ؼ�
		btn_back_introactivity.setOnClickListener(this);
		mMapView = (MapView) this.findViewById(R.id.mapview_activity);
		mMapView.setBuiltInZoomControls(true);// ���������������ſؼ�
		// ����һ����ͼ���������õ�mMapView�Ŀ���Ȩ���������������ƺ�����ƽ�ƺ�����
		MapController mMapController = mMapView.getController();
		mMapController.setZoom(18);// ���õ�ͼ�����ż���3-19��
		mMapController.enableClick(true);// ���õ�ͼ�Ƿ���Ӧ����¼�
		GeoPoint point = new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));// �ø����ľ�γ�ȹ���һ��GeoPoint����λ��΢�ȣ���*1E6��
		mMapController.setCenter(point);// ���õ�ͼ�����ĵ�
		// ����ItemizedOverlay
		itemOverlay = new ActivityOverlay(getResources().getDrawable(
				R.drawable.icon_marka), mMapView, IntroActivityMapActivity.this);
		Map<String, String> map = new HashMap<String, String>();
		map.put("action_flag", "findAll");
		String url = HttpUtil.BASE_URL + "servlet/ActivityServleat";
		// ʹ���첽���ø�����
		loadActivityAsyncTask = new LoadActivityAsyncTask();
		loadActivityAsyncTask.execute(map, url);

	}

	/**
	 * ���ø�����
	 * 
	 * @param map
	 *            ��ȡ���������ݵĲ���
	 * @param url
	 *            URL��ַ
	 * @return ActivityOverlay ������
	 */
	private ActivityOverlay initOverlay(Map<String, String> map, String url) {
		// �ӷ������л�ȡ���ݲ�ת��Ϊlist����
		List<Activities> list = new ArrayList<Activities>();
		try {
			String jsonString = HttpUtil.postRequest(url, map);
			if (jsonString != null) {
				list = JsonUtil.getMoreList(
						JsonUtil.getJsonValueByKey(jsonString, "activities"),
						Activities.class);
				for (int i = 0; i < list.size(); i++) {
					Activities activities = list.get(i);
					String location = activities.location;
					String name = activities.name;
					mark = drawBitmap(name);
					mark.setBounds(0, 0, mark.getIntrinsicWidth(),
							mark.getIntrinsicHeight());
					GeoPoint geoPoint = ChangeGeoPoint
							.StringToGeoPoint(location);
					OverlayItem item = new OverlayItem(geoPoint, name, "");
					item.setMarker(mark);
					itemOverlay.addItem(item);
				}
			} else {
				Toast.makeText(this, "û�����ӷ�����������", Toast.LENGTH_LONG).show();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return itemOverlay;
	}

	/**
	 * ���ø������ͼƬ
	 * 
	 * @param title
	 *            �������
	 * @return
	 */
	private Drawable drawBitmap(String title) {
		Bitmap bmp = Bitmap.createBitmap(250, 200, Bitmap.Config.ARGB_8888);// ���ݲ���������λͼ
		Canvas canvas = new Canvas(bmp);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.overlay_activity, null);
		TextView titleView = (TextView) layout
				.findViewById(R.id.tv_bubble_activityname);
		titleView.setText(title);
		layout.setDrawingCacheEnabled(true);// ����view���е�cache
		// measure()����view�Ĵ�С
		layout.measure(View.MeasureSpec.makeMeasureSpec(canvas.getWidth(),
				View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(
				canvas.getHeight(), View.MeasureSpec.EXACTLY));
		// layout()����view��λ��
		layout.layout(0, 0, layout.getMeasuredWidth(),
				layout.getMeasuredHeight());
		Paint paint = new Paint();
		canvas.drawBitmap(layout.getDrawingCache(), 0, 0, paint);// ʹ��getDrawingCache�����õ�view��cache��ͼƬ
		Drawable drawable = new BitmapDrawable(null, bmp);
		return drawable;
	}

	/**
	 * ��ť�ļ����¼�
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back_introactivity:
			this.finish();
			break;
		}
	}

	/**
	 * �첽�����ȡ��ע����Ϣ�����ñ�ע
	 * 
	 * @author wpy
	 * 
	 */
	private class LoadActivityAsyncTask extends
			AsyncTask<Object, Integer, ActivityOverlay> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected ActivityOverlay doInBackground(Object... params) {
			return initOverlay((Map<String, String>) params[0],
					(String) params[1]);
		}

		@Override
		protected void onPostExecute(ActivityOverlay result) {
			mMapView.getOverlays().add(result);// ��IteminizedOverlay��ӵ�MapView��
			mMapView.refresh();
		}
	}

	/**
	 * MapView������������Activityͬ��,������д���·���
	 */
	@Override
	protected void onDestroy() {
		/**
		 * MapView������������Activityͬ������activity����ʱ�����MapView.destroy()
		 */
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		/**
		 * MapView������������Activityͬ������activity����ʱ�����MapView.onPause()
		 */
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		/**
		 * MapView������������Activityͬ������activity�ָ�ʱ�����MapView.onResume()
		 */
		mMapView.onResume();
		super.onResume();
	}

}
