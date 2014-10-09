package com.wpy.map.common;

import com.baidu.platform.comapi.basestruct.GeoPoint;

public class ChangeGeoPoint {

	/**
	 * �ٶȵ�ͼ ��γ��lat�;���lngת��Ϊ΢�ȵ���������ת��ΪGeoPoint����
	 **/
	public static GeoPoint StringToGeoPoint(String coordinate) {
		String[] strings = coordinate.split(",");
		String lon = strings[0];
		String lat = strings[1];
		return new GeoPoint((int) (Double.parseDouble(lat) * 1E6),
				(int) (Double.parseDouble(lon) * 1E6));
	}

	/**
	 * ��String���͵�����ת����GeoPoint
	 * 
	 * @param pointString
	 * @return
	 */
	public static GeoPoint getGeoPoint(String pointString) {
		String[] s = pointString.split(",");
		int lon = Integer.parseInt(s[0]);
		int lat = Integer.parseInt(s[1]);
		return new GeoPoint(lat, lon);
	}
}
