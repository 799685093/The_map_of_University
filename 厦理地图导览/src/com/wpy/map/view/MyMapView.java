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
	 * ���췽��
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
	 * ��д�÷����õ���Ļ�����꣬�ڽ���Ļ����ת��Ϊ��������
	 */
	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		// ��õ����Ļ������
		int x = (int) arg0.getX();
		int y = (int) arg0.getY();
		// ������ת��Ϊ��������
		GeoPoint geoPoint = this.getProjection().fromPixels(x, y);
		// �����귢�͸���ע
		routeOverlay(geoPoint);
		return super.onTouchEvent(arg0);
	}

	/**
	 * �����ͼ�������ݣ���ʾ�û�ѡ���յ�
	 * 
	 * @param geoPoint
	 */
	public void routeOverlay(GeoPoint geoPoint) {
		// �����ע����Ϣ �������ҵ�λ�� �������û�ѡ���λ��
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
				R.drawable.icon_popup_normal);// ����ͼ��
		item.setMarker(mark);
		// ����һ��overlay
		GuideOverlay itemOverlay = new GuideOverlay(mark, this);
		this.getOverlays().clear();
		itemOverlay.addItem(item);
		this.getOverlays().add(itemOverlay);
		this.refresh();

	}
}
