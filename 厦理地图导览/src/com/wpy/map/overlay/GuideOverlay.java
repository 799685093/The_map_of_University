package com.wpy.map.overlay;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.wpy.map.common.ChangeGeoPoint;

public class GuideOverlay extends ItemizedOverlay<OverlayItem> {

	private static GeoPoint pointEnd;
	private static GeoPoint pointStart;
	public static boolean tapOverlay = false;

	/**
	 * 用MapView构造ItemizedOverlay
	 * 
	 * @param mark
	 *            显示在地图上的图标
	 * @param mapView
	 *            视图
	 */
	public GuideOverlay(Drawable mark, MapView mapView) {
		super(mark, mapView);
	}

	@Override
	public boolean onTap(GeoPoint arg0, MapView arg1) {
		return super.onTap(arg0, arg1);
	}

	/**
	 * 覆盖这个方法去处理一个item上的点击事件。
	 */
	@Override
	protected boolean onTap(int index) {
		tapOverlay = true;
		// RouteGuideActivity.et_end.setText("地图上的点");
		OverlayItem item = getItem(index);
		String myLocation = item.getTitle();
		String endLocation = item.getSnippet();
		pointStart = ChangeGeoPoint.getGeoPoint(myLocation);
		pointEnd = ChangeGeoPoint.getGeoPoint(endLocation);
		Log.d(">>>>>myLocation>>>>", myLocation);
		Log.d(">>>>>endLocation>>>>", endLocation);
		return super.onTap(index);
	}

	public static GeoPoint getpointStart() {
		return pointStart;

	}

	public static GeoPoint getpointEnd() {
		return pointEnd;

	}
}
