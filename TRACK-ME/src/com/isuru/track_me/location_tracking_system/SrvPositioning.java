package com.isuru.track_me.location_tracking_system;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SrvPositioning extends Service {

	private static final String TAG = "SrvPositioning";

	// An alarm for rising in special times to fire the
	// pendingIntentPositioning
	private AlarmManager alarmManagerPositioning;
	private SharedPreferences sPref;

	private int repetitions;
	// A PendingIntent for calling a receiver in special times
	public PendingIntent pendingIntentPositioning;

	@Override
	public void onCreate() {
		super.onCreate();

		Log.v(TAG, "Service Created");

		sPref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		repetitions = sPref.getInt("Frequency", 3);

		alarmManagerPositioning = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intentToFire = new Intent(
				ReceiverPositioningAlarm.ACTION_REFRESH_SCHEDULE_ALARM);
		intentToFire.putExtra(ReceiverPositioningAlarm.COMMAND,
				ReceiverPositioningAlarm.SENDER_SRV_POSITIONING);
		pendingIntentPositioning = PendingIntent.getBroadcast(this, 0,
				intentToFire, 0);

		ComponentName receiver = new ComponentName(getApplication(),
				ReceiverPositioningAlarm.class);

		PackageManager pm = getApplication().getPackageManager();

		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);

	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		final String requestor = intent.getStringExtra("sender");
		final boolean isRepeating = intent.getBooleanExtra("periodic", false);
		final String destination = intent.getStringExtra("destination");
		final int startID = startId;

		OnNewLocationListener onNewLocationListener = new OnNewLocationListener() {

			@Override
			public void onNewLocationReceived(Location location) {
				// use your new location here then stop listening

				Log.v(TAG, "Got Changed Location");

				Location currentLocation = ReceiverPositioningAlarm
						.getCurrentLocation();

				String longitude = String.valueOf(currentLocation
						.getLongitude());

				Log.v(TAG, "Longitude :" + longitude);
				Toast.makeText(getApplicationContext(),
						"Longitude :" + longitude, Toast.LENGTH_SHORT).show();

				SrvPositioning.this.onReceiveLocation(currentLocation,
						requestor, destination);

				ReceiverPositioningAlarm.clearOnNewLocationListener(this);

				if (!isRepeating || repetitions < 1) {

					ComponentName receiver = new ComponentName(
							getApplication(), ReceiverPositioningAlarm.class);

					PackageManager pm = getApplication().getPackageManager();

					pm.setComponentEnabledSetting(receiver,
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
							PackageManager.DONT_KILL_APP);

					// stop repeating
					alarmManagerPositioning.cancel(pendingIntentPositioning);

					SrvPositioning.this.stopSelfResult(startID);
				}

				repetitions--;

				sPref.edit().putInt("Frequency", repetitions).commit();

				Log.v(TAG, "Time To Live :" + repetitions);
			}
		};
		// start listening for new location
		ReceiverPositioningAlarm
				.setOnNewLocationListener(onNewLocationListener);

		Log.v(TAG, "Service Started");

		try {
			long interval = 60 * 1000;
			int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
			long timetoRefresh = SystemClock.elapsedRealtime();
			alarmManagerPositioning.setInexactRepeating(alarmType,
					timetoRefresh, interval, pendingIntentPositioning);
			Log.v(TAG, "Repeating Started");
		} catch (NumberFormatException e) {
			Toast.makeText(this, "error running service: " + e.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(this, "error running service: " + e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {

		Log.v(TAG, "Service Destroyed");

		this.alarmManagerPositioning.cancel(pendingIntentPositioning);
		ReceiverPositioningAlarm.stopLocationListener();
	}

	private void onReceiveLocation(Location location, String receiver,
			String destination) {
		Log.v(TAG, "Got the location");
		String currentLocation;
		if (location != null) {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			double speed = location.getSpeed();
			float[] distance = new float[3];

			String locationName = "";

			// Geo-code for the location (latitude + longitude)
			Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
			List<Address> addresses;
			try {
				addresses = gcd.getFromLocation(location.getLatitude(),
						location.getLongitude(), 1);

				if (addresses != null && addresses.size() > 0) {
					for (int i = 0; i < addresses.get(0)
							.getMaxAddressLineIndex(); i++) {
						locationName += (addresses.get(0).getAddressLine(i))
								.toString() + " ";
						Log.v(TAG, "Got address" + i + ") " + locationName);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			Log.v(TAG, "Checking dest availability : " + destination);

			double destLatitude = 0;
			double destLongitude = 0;

			if (!destination.equals("none")) {
				Log.v(TAG, "Destination available : " + destination);
				double[] destCordinates = getCordinates(destination);
				if (destCordinates.length == 2) {
					Log.v(TAG, "GeoCoded : " + destCordinates[0] + " "
							+ destCordinates[1]);
					destLatitude = destCordinates[0];
					destLongitude = destCordinates[1];
					Location.distanceBetween(latitude, longitude, destLatitude,
							destLongitude, distance);
				}
			}

			double[] inforSender = { longitude, latitude, destLongitude,
					destLatitude };

			String strDistance = "No distance info";
			if (distance != null) {
				strDistance = String.valueOf(distance[0]);
				Log.v(TAG, "Distance : " + strDistance);
			}
			// Formatting to send via an SMS
			currentLocation = "Latitude : " + latitude + "\nLonitude : "
					+ longitude + "\nCity : " + locationName + "\nSpeed : "
					+ speed;
			Log.v(TAG, "Got the location" + currentLocation);

			if (!locationName.equals("") && inforSender[2] != 0) {
				// start GoogleService service
				Intent serviceIntent = new Intent(getBaseContext(),
						GoogleService.class);
				serviceIntent.putExtra("currentLocation", currentLocation);
				serviceIntent.putExtra("locInfor", inforSender);
				serviceIntent.putExtra("receiver", receiver);
				// serviceIntent.putExtra("speed", speed);
				serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getBaseContext().startService(serviceIntent);
			} else {
				Log.v(TAG, "SMS not sent : Lack information");
			}

		} else {
			Log.v(TAG, "Got null location");
		}
	}

	private double[] getCordinates(String destination) {
		Geocoder coder = new Geocoder(getBaseContext(), Locale.getDefault());
		List<Address> address;
		double[] destCord = new double[2];

		try {
			address = coder.getFromLocationName(destination, 5);
			if (address == null) {
				destCord[0] = 0;
				destCord[1] = 0;
				return destCord;
			}
			Address location = address.get(0);
			destCord[0] = location.getLatitude();
			destCord[1] = location.getLongitude();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return destCord;
	}

}
