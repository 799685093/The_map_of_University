package com.wpy.map.overlay;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.tencentmap.streetviewsdk.animation.AnimGLSet;
import com.tencent.tencentmap.streetviewsdk.animation.ScaleResumeAnimGL;
import com.tencent.tencentmap.streetviewsdk.animation.TranslateAnimGL;
import com.tencent.tencentmap.streetviewsdk.overlay.ItemizedOverlay;
import com.tencent.tencentmap.streetviewsdk.overlay.model.ItemModel;
import com.wpy.map.activity.IntroDetailActivity;
import com.wpy.map.entity.StreetPoiData;

/**
 * 定义街景的覆盖物
 * 
 * @author wpy
 * 
 */
public class StreetOverlay extends ItemizedOverlay {

	private ArrayList<StreetPoiData> mPois;
	private Context context = null;

	public StreetOverlay(ArrayList<StreetPoiData> pois, Context context) {
		this.mPois = pois;
		this.context = context;
	}

	/**
	 * item点击时间
	 */
	@Override
	public void onTap(int index, float x, float y) {
		StreetPoiData data = mPois.get(index);
		Intent intent = new Intent(context, IntroDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("placename", data.name);
		intent.putExtras(bundle);
		context.startActivity(intent);
		Toast.makeText(context, data.name, Toast.LENGTH_LONG).show();
	}

	/**
	 * 根据索引得到item
	 */
	@Override
	public ItemModel getItem(int index) {
		final StreetPoiData poi = mPois.get(index);
		if (poi == null)
			return null;

		ItemModel item = new StreetItem(poi.latE6, poi.lonE6, poi.heightOffset);
		item.setAdapter(new ItemModel.IItemMarkerAdapter() {

			@Override
			public int getMarkerWidth() {
				return poi.marker.getWidth();
			}

			@Override
			public int getMarkerHeight() {
				return poi.marker.getHeight();
			}

			@Override
			public Bitmap getMarker(int state) {
				return poi.marker;
			}

			@Override
			public void onGetMarker(boolean suc) {

			}

			@Override
			public String getMarkerUID() {
				return null;
			}
		});

		TranslateAnimGL translateAnim = new TranslateAnimGL(0, 0, 180, 0, 400);
		ScaleResumeAnimGL scaleResumeAnimGL = new ScaleResumeAnimGL(1, 1, 1,
				0.7f, 100, 100);
		AnimGLSet animset = new AnimGLSet(translateAnim, scaleResumeAnimGL);
		item.startAnim(animset);
		return item;
	}

	/**
	 * 自定义一个Item 用来实现每个item的动画
	 * 
	 * @author michaelzuo
	 */
	private class StreetItem extends ItemModel {

		private float heightOffset;

		public StreetItem(int latE6, int lonE6, IItemMarkerAdapter adapter,
				float offset) {
			super(latE6, lonE6, adapter);
			this.heightOffset = offset;
		}

		public StreetItem(int latE6, int lonE6, float offset) {
			super(latE6, lonE6);
			this.heightOffset = offset;
		}

		@Override
		public float onGetItemScale(double distance, float angleScale) {
			/*
			 * 当distance < minDis , scale为maxScale 当distance > maxDis ,
			 * scale为minScale 当distance在minDis和maxDis之间 , scale按比例变化 当distance >
			 * maxShowDis , 不再显示
			 */
			float scale = 1.0f;
			// 根据视角poi进行缩放
			final float factor = 0.2f; // 根据视角放大倍数对Item缩放的因子
			scale = scale + (angleScale - 1) * factor;
			return scale;
		}

		@Override
		protected float onGetItemHeightOffset() {
			return this.heightOffset;
		}

	}

	/**
	 * 得到item的个数
	 */
	@Override
	public int size() {
		return mPois.size();
	}

}
