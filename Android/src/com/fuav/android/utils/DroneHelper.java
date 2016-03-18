package com.fuav.android.utils;

import android.content.res.Resources;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.coordinate.LatLong;

public class DroneHelper {

	final static double pi = 3.14159265358979324;
	final static double a = 6378245.0;
	final static double ee = 0.00669342162296594323;
	static double x_pi = pi * 3000.0 / 180.0;


	static public LatLng CoordToLatLang(LatLong coord) {
		return new LatLng(coord.getLatitude(), coord.getLongitude());
	}

    public static LatLong LatLngToCoord(LatLng point) {
        return new LatLong((float)point.latitude, (float) point.longitude);
    }

	public static LatLong LocationToCoord(Location location) {
		return new LatLong((float) location.getLatitude(), (float) location.getLongitude());
	}

	public static int scaleDpToPixels(double value, Resources res) {
		final float scale = res.getDisplayMetrics().density;
		return (int) Math.round(value * scale);
	}

//	public static LatLong BaiduLatLngToCoord(com.baidu.mapapi.model.LatLng point) {
//		double d[] = new double[2];
//		untransformBaidu(point.latitude,point.longitude,d);
//		return new LatLong(d[0],d[1]);
//	}
//
//	public static com.baidu.mapapi.model.LatLng CoordToBaiduLatLang(LatLong coord) {
//
//		double d[] = new double[2];
//		transformBaidu(coord.getLatitude(),coord.getLongitude(),d);
//		return new com.baidu.mapapi.model.LatLng(d[0],d[1]);
//
//	}
//
//	public static LatLong BDLocationToCoord(MyLocationData location){
//		double d[] = new double[2];
//		untransformBaidu(location.latitude,location.longitude,d);
//		return new LatLong(d[0],d[1]);
//	}

	public static com.amap.api.maps.model.LatLng CoordToGaodeLatLang(LatLong coord)
	{
		double d[] = new double[2];
		transform(coord.getLatitude(),coord.getLongitude(),d);
		return new com.amap.api.maps.model.LatLng(d[0],d[1]);
	}

	public static LatLong GaodeLatLngToCoord(com.amap.api.maps.model.LatLng point)
	{
		double d[] = new double[2];
		untransform(point.latitude, point.longitude, d);
		return new LatLong(d[0],d[1]);
	}


	public static void transformBaidu(double wgLat, double wgLon, double[] latlng)
	{
		double d[] = new double[2];
		transform(wgLat,wgLon,d);
		double x = d[1], y = d[0];
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
		latlng[1] = z * Math.cos(theta) + 0.0065;
		latlng[0] = z * Math.sin(theta) + 0.006;
	}

	public static void untransformBaidu(double wgLat, double wgLon, double[] latlng)
	{
		double x = wgLon - 0.0065, y = wgLat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
		double gg_lon = z * Math.cos(theta);
		double gg_lat = z * Math.sin(theta);
		untransform(gg_lat,gg_lon,latlng);
	}

	public static void transform(double wgLat, double wgLon, double[] latlng) {
		if (outOfChina(wgLat, wgLon)) {
			latlng[0] = wgLat;
			latlng[1] = wgLon;
			return;
		}
		double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
		double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
		double radLat = wgLat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		latlng[0] = wgLat + dLat;
		latlng[1] = wgLon + dLon;
	}

	public static void untransform(double wgLat, double wgLon, double[] latlng) {
		double d[] = new double[2];
		transform(wgLat,wgLon,d);
		latlng[0] = wgLat+(wgLat-d[0]);
		latlng[1] = wgLon+(wgLon-d[1]);
	}

	private static boolean outOfChina(double lat, double lon) {
		if (lon < 72.004 || lon > 137.8347)
			return true;
		if (lat < 0.8293 || lat > 55.8271)
			return true;
		return false;
	}

	private static double transformLat(double x, double y) {
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
		return ret;
	}

	private static double transformLon(double x, double y) {
		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
		return ret;
	}



}
