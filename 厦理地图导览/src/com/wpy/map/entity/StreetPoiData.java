package com.wpy.map.entity;

import android.graphics.Bitmap;

/**
 * ������Poi Overlay��������ݽ��з�װ
 * 
 * @author wpy
 * 
 */
public class StreetPoiData {

	/**
	 * γ�ȵ�1E6
	 */
	public int latE6;

	/**
	 * ���ȵ�1E6
	 */
	public int lonE6;

	/**
	 * poi��Ϣ����ʾ��ͼƬ
	 */
	public Bitmap marker;
	/**
	 * poi��Ϣ�������
	 */
	public String name;

	/**
	 * �߶�ƫ����
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
