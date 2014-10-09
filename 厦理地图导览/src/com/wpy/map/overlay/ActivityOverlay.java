package com.wpy.map.overlay;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.wpy.map.activity.IntroActivityDetailActivity;

public class ActivityOverlay extends ItemizedOverlay<OverlayItem> {

	Context mContext = null;

	/**
	 * 用MapView构造ItemizedOverlay
	 * 
	 * @param mark
	 *            显示在地图上的图标
	 * @param mapView
	 *            视图
	 * @param context
	 *            上下文
	 */
	public ActivityOverlay(Drawable mark, MapView mapView, Context context) {
		super(mark, mapView);
		mContext = context;
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		return super.onTap(p, mapView);
	}

	/**
	 * 覆盖这个方法去处理一个item上的点击事件。
	 */
	@Override
	protected boolean onTap(int index) {
		OverlayItem item = getItem(index);
		String name = item.getTitle();
		Intent intent = new Intent(mContext, IntroActivityDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("name", name);
		intent.putExtras(bundle);
		mContext.startActivity(intent);
		return super.onTap(index);
	}
}
