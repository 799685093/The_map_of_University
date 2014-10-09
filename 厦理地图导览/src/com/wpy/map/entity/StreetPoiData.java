package com.wpy.map.entity;

import android.graphics.Bitmap;

/**
 * 将创建Poi Overlay所需的数据进行封装
 * 
 * @author wpy
 * 
 */
public class StreetPoiData {

	/**
	 * 纬度的1E6
	 */
	public int latE6;

	/**
	 * 经度的1E6
	 */
	public int lonE6;

	/**
	 * poi信息点显示的图片
	 */
	public Bitmap marker;
	/**
	 * poi信息点的名称
	 */
	public String name;

	/**
	 * 高度偏移量
	 */
	public float heightOffset;

	public StreetPoiData(int x, int y, Bitmap marker, String name, float offset) {
		this.latE6 = x;
		this.lonE6 = y;
		this.marker = marker;
		this.heightOffset = offset;
		this.name = name;
	}
}
