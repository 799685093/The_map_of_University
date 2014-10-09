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
import com.wpy.map.entity.Place;
import com.wpy.map.overlay.PlaceOverlay;
import com.wpy.map.util.HttpUtil;
import com.wpy.map.util.JsonUtil;

/**
 * 校园地图介绍页面
 * 
 * @author wpy
 * 
 */
public class IntroActivity extends Activity implements OnClickListener {

	private MapView mMapView;// 显示地图的视图
	private ImageButton mBtnBack;
	// 厦门理工的经纬度118.093717,24.629962
	public double lon = 118.093717;// 经度
	public double lat = 24.629962;// 纬度

	private Drawable mark;// 标注物
	private PlaceOverlay itemOverlay;
	private LoadPlaceAsyncTask loadPlaceAsyncTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intromap);
		mBtnBack = (ImageButton) this.findViewById(R.id.intromap_btn_back);// 初始化控件
		mBtnBack.setOnClickListener(this);
		mMapView = (MapView) this.findViewById(R.id.mapview_guided);
		mMapView.setBuiltInZoomControls(true);// 设置启用内置缩放控件
		// 声明一个地图控制器，得到mMapView的控制权，可以用它来控制和驱动平移和缩放
		MapController mMapController = mMapView.getController();
		mMapController.setZoom(19);// 设置地图的缩放级别（3-19）
		mMapController.enableClick(true);// 设置地图是否相应点击事件
		GeoPoint point = new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));// 用给定的经纬度构造一个GeoPoint，单位是微度（度*1E6）
		mMapController.setCenter(point);// 设置地图的中心点
		// 创建ItemizedOverlay
		itemOverlay = new PlaceOverlay(getResources().getDrawable(
				R.drawable.icon_marka), mMapView, IntroActivity.this);
		Map<String, String> map = new HashMap<String, String>();
		map.put("action_flag", "findAll");
		String url = HttpUtil.BASE_URL + "servlet/PlaceServlet";
		// 使用异步设置覆盖物
		loadPlaceAsyncTask = new LoadPlaceAsyncTask();
		loadPlaceAsyncTask.execute(map, url);

	}

	/**
	 * 设置覆盖物
	 * 
	 * @param map
	 *            获取服务器数据的参数
	 * @param url
	 *            URL地址
	 * @return PlaceOverlay 覆盖物
	 */
	private PlaceOverlay initOverlay(Map<String, String> map, String url) {
		// 从服务器中获取数据并转换为list数组
		List<Place> list = new ArrayList<Place>();
		try {
			String jsonString = HttpUtil.postRequest(url, map);
			if (jsonString != null) {
				list = JsonUtil.getMoreList(
						JsonUtil.getJsonValueByKey(jsonString, "place"),
						Place.class);
				for (int i = 0; i < list.size(); i++) {
					Place place = list.get(i);
					String coordinate = place.coordinate;
					String placename = place.placename;
					mark = drawBitmap(placename);
					mark.setBounds(0, 0, mark.getIntrinsicWidth(),
							mark.getIntrinsicHeight());
					GeoPoint geoPoint = ChangeGeoPoint
							.StringToGeoPoint(coordinate);
					OverlayItem item = new OverlayItem(geoPoint, placename, "");
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
	 *            场所的名称
	 * @return
	 */
	private Drawable drawBitmap(String title) {
		Bitmap bmp = Bitmap.createBitmap(166, 100, Bitmap.Config.ARGB_8888);// 根据参数创建新位图
		Canvas canvas = new Canvas(bmp);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.overlay_text_view, null);
		TextView titleView = (TextView) layout.findViewById(R.id.tv_pop_title);
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
		case R.id.intromap_btn_back:
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
	private class LoadPlaceAsyncTask extends
			AsyncTask<Object, Integer, PlaceOverlay> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected PlaceOverlay doInBackground(Object... params) {
			return initOverlay((Map<String, String>) params[0],
					(String) params[1]);
		}

		@Override
		protected void onPostExecute(PlaceOverlay result) {
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
