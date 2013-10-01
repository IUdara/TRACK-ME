package com.isuru.track_me.location_tracking_system;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class ReceiverPositioningAlarm extends BroadcastReceiver {

	private static final String TAG = "ReceiverPositioningAlarm";

	public static final String COMMAND = "SENDER";
	public static final int SENDER_ACT_DOCUMENT = 0;
	public static final int SENDER_SRV_POSITIONING = 1;
	public static final int MIN_TIME_REQUEST = 5 * 1000;
	public static final String ACTION_REFRESH_SCHEDULE_ALARM = "com.isuru.track_me.location_tracking_system.ACTION_REFRESH_SCHEDULE_ALARM";
	private static Location currentLocation; // me - use getter method in
												// activity
	private static Location prevLocation;
	private static Context _context;
	private String provider = LocationManager.GPS_PROVIDER;
	@SuppressWarnings("unused")
	private static Intent _intent;
	private static LocationManager locationManager;
	private static LocationListener locationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

			Log.v(TAG, "Status Changed");

			try {
				String strStatus = "";
				switch (status) {
				case GpsStatus.GPS_EVENT_FIRST_FIX:
					strStatus = "GPS_EVENT_FIRST_FIX";
					break;
				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
					strStatus = "GPS_EVENT_SATELLITE_STATUS";
					break;
				case GpsStatus.GPS_EVENT_STARTED:
					strStatus = "GPS_EVENT_STARTED";
					break;
				case GpsStatus.GPS_EVENT_STOPPED:
					strStatus = "GPS_EVENT_STOPPED";
					break;
				default:
					strStatus = String.valueOf(status);
					break;
				}
				Toast.makeText(_context, "Status: " + strStatus,
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onLocationChanged(Location location) {

			Log.v(TAG, "Location Changed");

			try {
				Toast.makeText(_context, "***new location***",
						Toast.LENGTH_SHORT).show();
				gotLocation(location);
			} catch (Exception e) {
			}
		}
	};

	// received request from the calling service
	@Override
	public void onReceive(final Context context, Intent intent) {

		Log.v(TAG, "Received Request");

		Toast.makeText(context, "new request received by receiver",
				Toast.LENGTH_SHORT).show();
		_context = context;
		_intent = intent;
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(provider)) {
			locationManager.requestLocationUpdates(provider, MIN_TIME_REQUEST,
					5, locationListener);
			Location gotLoc = locationManager.getLastKnownLocation(provider);
			gotLocation(gotLoc);
		} else {
			Toast t = Toast.makeText(context, "please turn on GPS",
					Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
			Intent settinsIntent = new Intent(
					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			settinsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			_context.startActivity(settinsIntent);
		}
	}

	private static void gotLocation(Location location) {

		Log.v(TAG, "Got Location");

		prevLocation = currentLocation == null ? null : new Location(
				currentLocation);
		currentLocation = location;
		if (isLocationNew()) {
			OnNewLocationReceived(location);
			Toast.makeText(_context, "new location saved", Toast.LENGTH_SHORT)
					.show();
			stopLocationListener();
		} else {
			Log.v(TAG, "Not Changed Location");
		}
	}

	private static boolean isLocationNew() {
		if (currentLocation == null) {
			return false;
		} else if (prevLocation == null) {
			return true;
		} else if (currentLocation.getTime() == prevLocation.getTime()) {
			return false;
		} else {
			return true;
		}
	}

	public static void stopLocationListener() {

		Log.v(TAG, "Location Listener Stopped");

		locationManager.removeUpdates(locationListener);
		Toast.makeText(_context, "provider stoped", Toast.LENGTH_SHORT).show();
	}

	// listener ----------------------------------------------------
	static ArrayList<OnNewLocationListener> arrOnNewLocationListener = new ArrayList<OnNewLocationListener>();

	// Allows the user to set a OnNewLocationListener outside of this class
	// and react to the event.
	public static void setOnNewLocationListener(OnNewLocationListener listener) {
		arrOnNewLocationListener.add(listener);
	}

	public static void clearOnNewLocationListener(OnNewLocationListener listener) {
		arrOnNewLocationListener.remove(listener);
	}

	// This function is called after the new point received
	private static void OnNewLocationReceived(Location location) {
		// Check if the Listener was set, otherwise we'll get an Exception
		// when we try to call it
		if (arrOnNewLocationListener != null) {
			// Only trigger the event, when we have any listener
			for (int i = arrOnNewLocationListener.size() - 1; i >= 0; i--) {
				arrOnNewLocationListener.get(i).onNewLocationReceived(location);
			}
		}
	}

	public static Location getCurrentLocation() {

		Log.v(TAG, "Sending Location");

		return currentLocation;
	}

}
