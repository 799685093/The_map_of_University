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
 * ��ʾ����Ľ־�
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
		// ��ʼ���ؼ�
		mContainer = (LinearLayout) this.findViewById(R.id.street_layout);
		btn_back_street = (ImageButton) this.findViewById(R.id.btn_back_street);
		btn_back_street.setOnClickListener(this);
		btn_clear = (RelativeLayout) this.findViewById(R.id.btn_clear);
		btn_clear.setOnClickListener(this);
		btn_reset = (RelativeLayout) this.findViewById(R.id.btn_reset);
		btn_reset.setOnClickListener(this);

		// ��ȡҪ��ʾ�־��ľ�γ�� ������24.620608,118.084432 ͼ���24.623382,118.087591
		final double longitude = 24.623382;// ����
		final double latitude = 118.087591;// γ��
		GeoPoint center = new GeoPoint((int) (longitude * 1E6),
				(int) (latitude * 1E6));

		/**
		 * ͨ��������GeoPoint�����󷵻ؽ־�View. context - ������ p - �������ĵ� r - �����뾶 listener -
		 * ������ yawAngle - ��ʼƫ���� Ĭ��ֵΪ0 pitchAngle - ��ʼ������ Ĭ��ֵΪ0 key - ��Ȩkey
		 */
		StreetViewShow.getInstance().showStreetView(this, center, 100, this,
				-170, 0);
	}

	// ��ȡ��Ӧ�Ľ־���activity����ʾ
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

	// �ڲ����õ��־�View������Ҫ������Դʱ������onDestroy()��������
	@Override
	protected void onDestroy() {
		StreetViewShow.getInstance().destory();
		super.onDestroy();
	}

	// �־��������
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
	 * ��ʾ�־��ĸ�����
	 */
	@Override
	public ItemizedOverlay getOverlay() {
		if (overlay == null) {
			// �ӷ������л�ȡ���ݲ�ת��Ϊlist����

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
				String placename = place.placename; // ��������ת��
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
			// String test = "����";
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
	 * ���ø������λͼ
	 * 
	 * @param name
	 * @return
	 */
	private Bitmap getBm(String name) {
		Bitmap bmp = Bitmap.createBitmap(166, 166, Bitmap.Config.ARGB_8888);// ���ݲ���������λͼ
		Canvas canvas = new Canvas(bmp);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.street_overlay, null);
		TextView titleView = (TextView) layout
				.findViewById(R.id.street_overlay_tv);
		titleView.setText(name);
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
		return bmp;
	}

	/**
	 * ��ť�ĵ���¼�
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back_street:
			this.finish();
			break;
		case R.id.btn_clear:
			overlay.clear();
			Toast.makeText(this, "����ɹ�", Toast.LENGTH_SHORT).show();
			break;
		case R.id.btn_reset:
			overlay.populate();
			break;
		}
	}

	// ��Ȩʧ��
	@Override
	public void onAuthFail() {

	}

	// �������ݽ�������
	@Override
	public void onDataError() {
		Toast.makeText(this, "���ݽ������������ԣ�", Toast.LENGTH_SHORT).show();
	}

	// �����������
	@Override
	public void onNetError() {
		Toast.makeText(this, "�������ӳ������Ժ�����", Toast.LENGTH_LONG).show();
	}

	/**
	 * ʵ�ֽ�ͼ
	 * 
	 * @param activity
	 */
	/*
	 * public void setpic(Activity activity) { // View������Ҫ��ͼ��View View view =
	 * activity.getWindow().getDecorView(); view.setDrawingCacheEnabled(true);
	 * view.buildDrawingCache(); Bitmap bitmap = view.getDrawingCache(); //
	 * ��ȡ״̬���߶� Rect frame = new Rect();
	 * activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
	 * int toHeight = frame.top; // ��ȡ��Ļ�ķֱ��� DisplayMetrics displayMetrics = new
	 * DisplayMetrics(); activity.getWindowManager().getDefaultDisplay()
	 * .getMetrics(displayMetrics); // ��ȡ��Ļ�Ŀ�͸� int width =
	 * displayMetrics.widthPixels; int height = displayMetrics.heightPixels;
	 * bitmap = Bitmap.createBitmap(bitmap, 0, toHeight, width, height -
	 * toHeight); try { FileOutputStream fout = new FileOutputStream(
	 * "mnt/sdcard/mytttttt.png"); bitmap.compress(Bitmap.CompressFormat.PNG,
	 * 100, fout); } catch (FileNotFoundException e) { e.printStackTrace(); }
	 * view.setDrawingCacheEnabled(false); }
	 */
}
