package com.isuru.track_me.location_tracking_system;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.isuru.track_me.location_tracking_system.MyLocation.LocationResult;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

//Turn on/off GPS radio automatically - Retrieve information
public class PositionManager {

	// private static PositionManager gir;
	private Context cntxt;
	private static final String TAG = "PositionManager";
	private LocationManager locationMangaer = null;
	private LocationListener locationListener = null;
	private Boolean flag = false;
	private String currentLocation;

	PositionManager(Context context) {
		cntxt = context;
		locationMangaer = (LocationManager) cntxt
				.getSystemService(Context.LOCATION_SERVICE);
		currentLocation = null;
	}

	private class MyLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location loc) {

			Log.v(TAG, "Changed the location");

			String longitude = "Longitude: " + loc.getLongitude();
			Log.v(TAG, longitude);
			String latitude = "Latitude: " + loc.getLatitude();
			Log.v(TAG, latitude);

			/*----------to get City-Name from coordinates ------------- */
			String cityName = null;
			Geocoder gcd = new Geocoder(cntxt, Locale.getDefault());
			List<Address> addresses;
			try {
				addresses = gcd.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
				if (addresses.size() > 0)
					System.out.println(addresses.get(0).getLocality());
				cityName = addresses.get(0).getLocality();
			} catch (IOException e) {
				e.printStackTrace();
			}

			currentLocation = longitude + "\n" + latitude
					+ "\n\nMy Currrent City is: " + cityName;

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

		}
	}

	public void getCurrentLocation() {
		// TODO Auto-generated method stub
		Log.v(TAG, "Tried to get location");

		// flag = displayGpsStatus();
		// if (flag) {
		//
		// Log.v(TAG, "GPS on");
		//
		// locationListener = new MyLocationListener();
		//
		// locationMangaer.requestLocationUpdates(
		// LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
		//
		// } else {
		// Log.v(TAG, "GPS off");
		// currentLocation = "good";
		// }
		// **********************************************
		// GPSManager mGPS = new GPSManager(cntxt);
		//
		// if (mGPS.canGetLocation) {
		// // Log.v(TAG, "GPS on");
		// mGPS.getLocation();
		// currentLocation = "Lat" + mGPS.getLatitude() + "Lon"
		// + mGPS.getLongitude();
		// } else {
		// Log.v(TAG, "GPS off");
		// currentLocation = "Unabletofind";
		// }
		// ********************************************
//		LocationResult locationResult = new LocationResult() {
//			@Override
//			public void gotLocation(Location location) {
//				// Got the location!
//				Log.v(TAG, "Got the location");
//				
//				if (location != null) {
//					double latitude = location.getLatitude();
//					double longitude = location.getLongitude();
//					currentLocation = "Lat" + latitude + "Lon" + longitude;
//				} else {
//					currentLocation = "Unabletofind";
//				}
//			}
//		};
//		MyLocation myLocation = new MyLocation();
//		Log.v(TAG, "Pending location");
//		myLocation.getLocation(cntxt, locationResult);
//		Log.v(TAG, "Location OK" + currentLocation);
		
//		if (currentLocation) {
//			
//		} else {
//
//		}
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
