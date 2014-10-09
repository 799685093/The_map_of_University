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
	 * ��MapView����ItemizedOverlay
	 * 
	 * @param mark
	 *            ��ʾ�ڵ�ͼ�ϵ�ͼ��
	 * @param mapView
	 *            ��ͼ
	 */
	public GuideOverlay(Drawable mark, MapView mapView) {
		super(mark, mapView);
	}

	@Override
	public boolean onTap(GeoPoint arg0, MapView arg1) {
		return super.onTap(arg0, arg1);
	}

	/**
	 * �����������ȥ����һ��item�ϵĵ���¼���
	 */
	@Override
	protected boolean onTap(int index) {
		tapOverlay = true;
		// RouteGuideActivity.et_end.setText("��ͼ�ϵĵ�");
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
