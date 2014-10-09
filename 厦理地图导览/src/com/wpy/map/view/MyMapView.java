package com.wpy.map.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.wpy.map.R;
import com.wpy.map.overlay.GuideOverlay;

public class MyMapView extends MapView {
	private final String TAG = "MyMapView";
	private GeoPoint locationPoint;

	/**
	 * 构造方法
	 * 
	 * @param context
	 */
	public MyMapView(Context context) {
		super(context);
	}

	public MyMapView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
	}

	public MyMapView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	public void setlocationPoint(GeoPoint g) {
		this.locationPoint = g;
	}

	/**
	 * 重写该方法得到屏幕的坐标，在将屏幕坐标转换为地理坐标
	 */
	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		// 获得点击屏幕的坐标
		int x = (int) arg0.getX();
		int y = (int) arg0.getY();
		// 将坐标转化为地理坐标
		GeoPoint geoPoint = this.getProjection().fromPixels(x, y);
		// 将坐标发送给标注
		routeOverlay(geoPoint);
		return super.onTouchEvent(arg0);
	}

	/**
	 * 点击地图弹出气泡，提示用户选择终点
	 * 
	 * @param geoPoint
	 */
	public void routeOverlay(GeoPoint geoPoint) {
		// 定义标注的信息 标题是我的位置 内容是用户选择的位置
		OverlayItem item = new OverlayItem(geoPoint,
				Integer.toString(locationPoint.getLongitudeE6()) + ","
						+ Integer.toString(locationPoint.getLatitudeE6()),
				Integer.toString(geoPoint.getLongitudeE6()) + ","
						+ Integer.toString(geoPoint.getLatitudeE6()));
		Log.d(TAG, "mylon" + Integer.toString(locationPoint.getLongitudeE6()));
		Log.d(TAG, "mylat" + Integer.toString(locationPoint.getLatitudeE6()));
		Log.d(TAG, "endlon" + Integer.toString(geoPoint.getLongitudeE6()));
		Log.d(TAG, "endlat" + Integer.toString(geoPoint.getLatitudeE6()));
		Drawable mark = this.getResources().getDrawable(
				R.drawable.icon_popup_normal);// 气泡图标
		item.setMarker(mark);
		// 创建一个overlay
		GuideOverlay itemOverlay = new GuideOverlay(mark, this);
		this.getOverlays().clear();
		itemOverlay.addItem(item);
		this.getOverlays().add(itemOverlay);
		this.refresh();

	}
}
