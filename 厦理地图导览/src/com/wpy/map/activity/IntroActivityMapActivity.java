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
 * 校园活动地图页面
 * 
 * @author wpy
 * 
 */
public class IntroActivityMapActivity extends Activity implements
		OnClickListener {

	private MapView mMapView;// 显示地图的视图
	private ImageButton btn_back_introactivity;
	// 厦门理工的经纬度118.093717,24.629962
	public double lon = 118.093717;// 经度
	public double lat = 24.629962;// 纬度

	private Drawable mark;// 标注物
	private ActivityOverlay itemOverlay;
	private LoadActivityAsyncTask loadActivityAsyncTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_introactivity);
		btn_back_introactivity = (ImageButton) this
				.findViewById(R.id.btn_back_introactivity);// 初始化控件
		btn_back_introactivity.setOnClickListener(this);
		mMapView = (MapView) this.findViewById(R.id.mapview_activity);
		mMapView.setBuiltInZoomControls(true);// 设置启用内置缩放控件
		// 声明一个地图控制器，得到mMapView的控制权，可以用它来控制和驱动平移和缩放
		MapController mMapController = mMapView.getController();
		mMapController.setZoom(18);// 设置地图的缩放级别（3-19）
		mMapController.enableClick(true);// 设置地图是否相应点击事件
		GeoPoint point = new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));// 用给定的经纬度构造一个GeoPoint，单位是微度（度*1E6）
		mMapController.setCenter(point);// 设置地图的中心点
		// 创建ItemizedOverlay
		itemOverlay = new ActivityOverlay(getResources().getDrawable(
				R.drawable.icon_marka), mMapView, IntroActivityMapActivity.this);
		Map<String, String> map = new HashMap<String, String>();
		map.put("action_flag", "findAll");
		String url = HttpUtil.BASE_URL + "servlet/ActivityServleat";
		// 使用异步设置覆盖物
		loadActivityAsyncTask = new LoadActivityAsyncTask();
		loadActivityAsyncTask.execute(map, url);

	}

	/**
	 * 设置覆盖物
	 * 
	 * @param map
	 *            获取服务器数据的参数
	 * @param url
	 *            URL地址
	 * @return ActivityOverlay 覆盖物
	 */
	private ActivityOverlay initOverlay(Map<String, String> map, String url) {
		// 从服务器中获取数据并转换为list数组
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
				Toast.makeText(this, "没有连接服务器，请检测", Toast.LENGTH_LONG).show();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return itemOverlay;
	}

	/**
	 * 设置覆盖物的图片
	 * 
	 * @param title
	 *            活动的名称
	 * @return
	 */
	private Drawable drawBitmap(String title) {
		Bitmap bmp = Bitmap.createBitmap(250, 200, Bitmap.Config.ARGB_8888);// 根据参数创建新位图
		Canvas canvas = new Canvas(bmp);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.overlay_activity, null);
		TextView titleView = (TextView) layout
				.findViewById(R.id.tv_bubble_activityname);
		titleView.setText(title);
		layout.setDrawingCacheEnabled(true);// 开启view当中的cache
		// measure()设置view的大小
		layout.measure(View.MeasureSpec.makeMeasureSpec(canvas.getWidth(),
				View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(
				canvas.getHeight(), View.MeasureSpec.EXACTLY));
		// layout()设置view的位置
		layout.layout(0, 0, layout.getMeasuredWidth(),
				layout.getMeasuredHeight());
		Paint paint = new Paint();
		canvas.drawBitmap(layout.getDrawingCache(), 0, 0, paint);// 使用getDrawingCache方法得到view的cache的图片
		Drawable drawable = new BitmapDrawable(null, bmp);
		return drawable;
	}

	/**
	 * 按钮的监听事件
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
	 * 异步任务获取标注的信息及设置标注
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
			mMapView.getOverlays().add(result);// 将IteminizedOverlay添加到MapView中
			mMapView.refresh();
		}
	}

	/**
	 * MapView的生命周期与Activity同步,所以重写以下方法
	 */
	@Override
	protected void onDestroy() {
		/**
		 * MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		 */
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		/**
		 * MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		 */
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		/**
		 * MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		 */
		mMapView.onResume();
		super.onResume();
	}

}
