package com.isuru.track_me.location_tracking_system;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.isuru.track_me.location_tracking_system.MyLocation.LocationResult;
import com.isuru.track_me.sms_handling_system.SMSSender;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class PositionNotifier extends Service {

	private static final String TAG = "PositionNotifier";

	private SMSSender smsSender;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		smsSender = new SMSSender();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent.getStringExtra("sender") != null) {

			final String receiver = intent.getStringExtra("sender");

			LocationResult locationResult = new LocationResult(receiver) {
				@Override
				public void gotLocation(Location location) {
					// Got the location!
					Log.v(TAG, "Got the location");
					String currentLocation;
					if (location != null) {
						double latitude = location.getLatitude();
						double longitude = location.getLongitude();

						String locationName = "";
						Geocoder gcd = new Geocoder(getBaseContext(),
								Locale.getDefault());
						List<Address> addresses;
						try {
							addresses = gcd.getFromLocation(
									location.getLatitude(),
									location.getLongitude(), 1);

							if (addresses != null && addresses.size() > 0) {
								for (int i = 0; i < addresses.get(0)
										.getMaxAddressLineIndex(); i++) {
									locationName += (addresses.get(0)
											.getAddressLine(i)).toString()
											+ " ";
									Log.v(TAG, "Got address" + i + ") "
											+ locationName);
								}
							}

						} catch (IOException e) {
							e.printStackTrace();
						}

						currentLocation = "Latitude : " + latitude
								+ "\nLonitude : " + longitude + "\nCity : "
								+ locationName;
						Log.v(TAG, "Got the location" + currentLocation);
						smsSender.sendSMS(receiver, currentLocation);
					} else {
						Log.v(TAG, "Got null location");
					}

				}
			};
			MyLocation myLocation = new MyLocation();
			Log.v(TAG, "Pending location");
			myLocation.getLocation(getBaseContext(), locationResult);
			Log.v(TAG, "Location OK");
		}

		this.stopSelf();

		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		Toast.makeText(this, "SMSManager Service Destroy", Toast.LENGTH_LONG)
				.show();
	}

	// Method for testing
	@SuppressWarnings("unused")
	private void setMockLocation(LocationManager lm, double latitude,
			double longitude, float accuracy) {
		lm.addTestProvider(LocationManager.GPS_PROVIDER,
				"requiresNetwork" == "", "requiresSatellite" == "",
				"requiresCell" == "", "hasMonetaryCost" == "",
				"supportsAltitude" == "", "supportsSpeed" == "",
				"supportsBearing" == "", android.location.Criteria.POWER_LOW,
				android.location.Criteria.ACCURACY_FINE);

		Location newLocation = new Location(LocationManager.GPS_PROVIDER);

		newLocation.setLatitude(latitude);
		newLocation.setLongitude(longitude);
		newLocation.setAccuracy(accuracy);

		lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

		lm.setTestProviderStatus(LocationManager.GPS_PROVIDER,
				LocationProvider.AVAILABLE, null, System.currentTimeMillis());

		lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);
	}
}
