package com.wpy.map.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.tencentmap.streetviewsdk.StreetViewListener;
import com.tencent.tencentmap.streetviewsdk.StreetViewShow;
import com.tencent.tencentmap.streetviewsdk.map.basemap.GeoPoint;
import com.tencent.tencentmap.streetviewsdk.overlay.ItemizedOverlay;
import com.wpy.map.R;
import com.wpy.map.entity.Place;
import com.wpy.map.entity.StreetPoiData;
import com.wpy.map.overlay.StreetOverlay;
import com.wpy.map.util.HttpUtil;
import com.wpy.map.util.JsonUtil;

/**
 * 显示厦理的街景
 * 
 * @author wpy
 * 
 */
public class StreetViewActivity extends Activity implements StreetViewListener,
		OnClickListener {

	private View mStreetView;
	private ViewGroup mContainer;
	private ArrayList<StreetPoiData> pois;
	private StreetOverlay overlay;
	private ImageButton btn_back_street;
	private RelativeLayout btn_clear;
	private RelativeLayout btn_reset;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_streetview);
		// 初始化控件
		mContainer = (LinearLayout) this.findViewById(R.id.street_layout);
		btn_back_street = (ImageButton) this.findViewById(R.id.btn_back_street);
		btn_back_street.setOnClickListener(this);
		btn_clear = (RelativeLayout) this.findViewById(R.id.btn_clear);
		btn_clear.setOnClickListener(this);
		btn_reset = (RelativeLayout) this.findViewById(R.id.btn_reset);
		btn_reset.setOnClickListener(this);

		// 获取要显示街景的经纬度 理工南门24.620608,118.084432 图书馆24.623382,118.087591
		final double longitude = 24.623382;// 经度
		final double latitude = 118.087591;// 纬度
		GeoPoint center = new GeoPoint((int) (longitude * 1E6),
				(int) (latitude * 1E6));

		/**
		 * 通过给出的GeoPoint吸附后返回街景View. context - 上下文 p - 吸附中心点 r - 吸附半径 listener -
		 * 监听器 yawAngle - 初始偏航角 默认值为0 pitchAngle - 初始俯仰角 默认值为0 key - 鉴权key
		 */
		StreetViewShow.getInstance().showStreetView(this, center, 100, this,
				-170, 0);
	}

	// 获取对应的街景到activity中显示
	@Override
	public void onViewReturn(final View v) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mStreetView = v;
				mContainer.addView(mStreetView);
			}
		});
	}

	// 在不再用到街景View，即需要销毁资源时，调用onDestroy()进行销毁
	@Override
	protected void onDestroy() {
		StreetViewShow.getInstance().destory();
		super.onDestroy();
	}

	// 街景加载完毕
	@Override
	public void onLoaded() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mStreetView.setVisibility(View.VISIBLE);
			}
		});
	}

	/**
	 * 显示街景的覆盖物
	 */
	@Override
	public ItemizedOverlay getOverlay() {
		if (overlay == null) {
			// 从服务器中获取数据并转换为list数组

			Map<String, String> map = new HashMap<String, String>();
			map.put("action_flag", "findAll");
			String url = HttpUtil.BASE_URL + "servlet/PlaceServlet";
			List<Place> list = new ArrayList<Place>();
			try {
				String jsonString = HttpUtil.postRequest(url, map);
				list = JsonUtil.getMoreList(
						JsonUtil.getJsonValueByKey(jsonString, "place"),
						Place.class);

			} catch (Exception e) {
				e.printStackTrace();
			}
			pois = new ArrayList<StreetPoiData>();
			for (int i = 0; i < list.size(); i++) {
				Place place = list.get(i);
				String placename = place.placename; // 进行坐标转换
				String[] s = place.coordinate.split(",");
				String translateUrl = "http://apis.map.qq.com/ws/coord/v1/translate?locations="
						+ s[1]
						+ ","
						+ s[0]
						+ "&type=3&key=EDEBZ-ECYRU-O3WVR-23BYM-5IKC6-6MFZF";
				try {
					String jsons = HttpUtil.getRequest(translateUrl);
					JSONObject jsonObject = new JSONObject(jsons);
					int status = jsonObject.getInt("status");
					if (status == 0) {
						JSONArray array = jsonObject.getJSONArray("locations");
						JSONObject jsonObject2 = array.getJSONObject(0);
						int lat = (int) (Double.parseDouble(jsonObject2
								.getString("lat")) * 1E6);
						int lon = (int) (Double.parseDouble(jsonObject2
								.getString("lng")) * 1E6);
						pois.add(new StreetPoiData(lat, lon, getBm(placename),
								placename, 0));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// pois = new ArrayList<StreetPoiData>();
			// String test = "测试";
			// pois.add(new StreetPoiData(
			// (int) (Double.parseDouble("24.62361259") * 1E6),
			// (int) (Double.parseDouble("118.08730653") * 1E6),
			// getBm(test), test, 120));
			overlay = new StreetOverlay(pois, StreetViewActivity.this);
			overlay.populate();
		}
		return overlay;
	}

	/**
	 * 设置覆盖物的位图
	 * 
	 * @param name
	 * @return
	 */
	private Bitmap getBm(String name) {
		Bitmap bmp = Bitmap.createBitmap(166, 166, Bitmap.Config.ARGB_8888);// 根据参数创建新位图
		Canvas canvas = new Canvas(bmp);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.street_overlay, null);
		TextView titleView = (TextView) layout
				.findViewById(R.id.street_overlay_tv);
		titleView.setText(name);
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
		return bmp;
	}

	/**
	 * 按钮的点击事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back_street:
			this.finish();
			break;
		case R.id.btn_clear:
			overlay.clear();
			Toast.makeText(this, "清除成功", Toast.LENGTH_SHORT).show();
			break;
		case R.id.btn_reset:
			overlay.populate();
			break;
		}
	}

	// 鉴权失败
	@Override
	public void onAuthFail() {

	}

	// 发生数据解析错误
	@Override
	public void onDataError() {
		Toast.makeText(this, "数据解析出错，请重试！", Toast.LENGTH_SHORT).show();
	}

	// 发生网络错误
	@Override
	public void onNetError() {
		Toast.makeText(this, "网络连接出错，请稍后重试", Toast.LENGTH_LONG).show();
	}

	/**
	 * 实现截图
	 * 
	 * @param activity
	 */
	/*
	 * public void setpic(Activity activity) { // View是你需要截图的View View view =
	 * activity.getWindow().getDecorView(); view.setDrawingCacheEnabled(true);
	 * view.buildDrawingCache(); Bitmap bitmap = view.getDrawingCache(); //
	 * 获取状态栏高度 Rect frame = new Rect();
	 * activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
	 * int toHeight = frame.top; // 获取屏幕的分辨率 DisplayMetrics displayMetrics = new
	 * DisplayMetrics(); activity.getWindowManager().getDefaultDisplay()
	 * .getMetrics(displayMetrics); // 获取屏幕的宽和高 int width =
	 * displayMetrics.widthPixels; int height = displayMetrics.heightPixels;
	 * bitmap = Bitmap.createBitmap(bitmap, 0, toHeight, width, height -
	 * toHeight); try { FileOutputStream fout = new FileOutputStream(
	 * "mnt/sdcard/mytttttt.png"); bitmap.compress(Bitmap.CompressFormat.PNG,
	 * 100, fout); } catch (FileNotFoundException e) { e.printStackTrace(); }
	 * view.setDrawingCacheEnabled(false); }
	 */
}
