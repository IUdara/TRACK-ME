package com.isuru.track_me.location_tracking_system;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.isuru.track_me.authentication_system.Authenticator.UserLoginTask;
import com.isuru.track_me.location_tracking_system.MyLocation.LocationResult;
import com.isuru.track_me.sms_handling_system.SMSSender;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class PositionNotifier extends Service {

	private static final String TAG = "PositionNotifier";

	private GeoCodingTask mGeoTask = null;

	private PositionManager positionMangaer;
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
		positionMangaer = new PositionManager(this.getBaseContext());
		smsSender = new SMSSender();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent.getStringExtra("sender") != null) {
			// String location = positionMangaer.getCurrentLocation();
			// // int i = 0;
			// // while (location.equals("Unabletofind") && i < 10) {
			// // i++;
			// // location = positionMangaer.getCurrentLocation();
			// // Log.v(TAG, "Looping");
			// // }
			// if (!location.equals("Unabletofind") || location != null) {
			// Log.v(TAG, "1) Location acquired" + location +
			// intent.getStringExtra("sender"));
			// smsSender.sendSMS(intent.getStringExtra("sender"), location);
			// Log.v(TAG, "Location acquired");
			// }

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

						String cityName = null;
						Geocoder gcd = new Geocoder(getBaseContext(),
								Locale.getDefault());
						List<Address> addresses;
						try {
							addresses = gcd.getFromLocation(
									location.getLatitude(),
									location.getLongitude(), 1);

							if (addresses != null && addresses.size() > 0)
								System.out.println(addresses.get(0)
										.getLocality());
							cityName = addresses.get(0).getLocality();

						} catch (IOException e) {
							e.printStackTrace();
						}

						currentLocation = "Latitude : " + latitude
								+ "/nLonitude : " + longitude + "/nCity : "
								+ cityName;
						Log.v(TAG, "Got the location" + currentLocation);
						smsSender.sendSMS(receiver, currentLocation);
					} else {
						Log.v(TAG, "Got null location");

						LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

						lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
								0, 0, new LocationListener() {
									@Override
									public void onStatusChanged(
											String provider, int status,
											Bundle extras) {
									}

									@Override
									public void onProviderEnabled(
											String provider) {
									}

									@Override
									public void onProviderDisabled(
											String provider) {
									}

									@Override
									public void onLocationChanged(
											Location location) {
									}
								});

						/* Set a mock location for debugging purposes */
						setMockLocation(lm, 15.387653, 73.872585, 500);

						location = lm
								.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if (location != null) {
							double latitude = location.getLatitude();
							double longitude = location.getLongitude();

							String cityName = null;
							Geocoder gcd = new Geocoder(getBaseContext(),
									Locale.getDefault());
							List<Address> addresses;
							try {
								addresses = gcd.getFromLocation(
										location.getLatitude(),
										location.getLongitude(), 1);

								if (addresses != null && addresses.size() > 0)
									System.out.println(addresses.get(0)
											.getLocality());
								cityName = addresses.get(0).getLocality();

							} catch (IOException e) {
								e.printStackTrace();
							}

							currentLocation = "Latitude : " + latitude
									+ "/nLonitude : " + longitude + "/nCity : "
									+ cityName;
							Log.v(TAG, "Got the location" + currentLocation);
							smsSender.sendSMS(receiver, currentLocation);
						}
					}

					// if (location != null) {
					// Log.v(TAG, "Got not null location");
					// mGeoTask = new GeoCodingTask();
					// mGeoTask.execute(location, receiver);
					// }else{
					// LocationManager lm =
					// (LocationManager)getSystemService(Context.LOCATION_SERVICE);
					//
					// lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					// 0, 0, new LocationListener() {
					// @Override
					// public void onStatusChanged(String provider, int status,
					// Bundle extras) {}
					// @Override
					// public void onProviderEnabled(String provider) {}
					// @Override
					// public void onProviderDisabled(String provider) {}
					// @Override
					// public void onLocationChanged(Location location) {}
					// });
					//
					// /* Set a mock location for debugging purposes */
					// setMockLocation(lm, 15.387653, 73.872585, 500);
					// Location loc =
					// lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					// mGeoTask = new GeoCodingTask();
					// mGeoTask.execute(loc, receiver);
					// }
					Log.v(TAG, "Execution Finished");
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

	private void setMockLocation(LocationManager lm, double latitude,
			double longitude, float accuracy) {
		// LocationManager lm = (LocationManager)
		// getBaseContext().getSystemService(Context.LOCATION_SERVICE);
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

	public class GeoCodingTask extends AsyncTask<Object, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Object... params) {
			String result = null;
			int responseCount = 0;
			do {
				try {
					Geocoder geocoder = new Geocoder(getBaseContext(),
							Locale.getDefault());
					Location loc = (Location) params[0];

					double latitude = loc.getLatitude();
					double longitude = loc.getLongitude();

					List<Address> addresses = geocoder.getFromLocation(
							latitude, longitude, 1);

					if (addresses != null && addresses.size() > 0) {
						Address address = addresses.get(0);
						// result = String.format(
						// "%s, %s, %s",
						// address.getMaxAddressLineIndex() > 0 ? address
						// .getAddressLine(0) : "", address.getLocality(),
						// address.getCountryName());

						String district = address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "";
						if (!"".equals(district)) {
							district = district.substring(
									district.lastIndexOf(",") + 2,
									district.length());
						}

						result = String.format("%s, %s, %.4f, %.4f", district,
								address.getLocality(), latitude, longitude);
					}

				} catch (IOException e) {
					e.printStackTrace();

				} finally {
					responseCount++;
				}

			} while (result == null || responseCount <= 3);

			smsSender.sendSMS((String) params[1], result);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mGeoTask = null;
		}

	}
}
