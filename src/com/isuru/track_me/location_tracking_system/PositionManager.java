package com.isuru.track_me.location_tracking_system;

import android.content.ContentResolver;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

//Turn on/off GPS radio automatically - Retrieve information
public class PositionManager{
	
//	private static PositionManager gir;	
	private Context cntxt;
	private static final String TAG = "Debug";
	private LocationManager locationMangaer=null;
	private LocationListener locationListener=null;
	private Boolean flag = false;

	PositionManager(Context context) {
		cntxt = context;
		locationMangaer = (LocationManager) cntxt.getSystemService(Context.LOCATION_SERVICE);
	}

//	public static PositionManager getGIR() {
//		if (gir == null) {
//			gir = new PositionManager();
//		}
//		return gir;
//	}

//	
//
//	public void getCurrentLocation() {
//
//	}
//
//	public void getDistance() {
//
//	}
//
//	public void getTime() {
//
//	}

	public void getCurrentLocation() {
		// TODO Auto-generated method stub
		flag = displayGpsStatus();
		if (flag) {
			
			Log.v(TAG, "onClick");		
			
			locationListener = new GPSManager(cntxt);

			locationMangaer.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10,
	                locationListener);
			
			
			
			} 
		
	}

	private Boolean displayGpsStatus() {
		ContentResolver contentResolver = cntxt.getContentResolver();
		boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(
				contentResolver, LocationManager.GPS_PROVIDER);
		if (gpsStatus) {
			return true;

		} else {
			return false;
		}
	}
}
