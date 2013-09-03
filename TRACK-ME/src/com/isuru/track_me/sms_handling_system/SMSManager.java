package com.isuru.track_me.sms_handling_system;

import com.isuru.track_me.location_tracking_system.PositionNotifier;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class SMSManager extends Service {
	
	private static final String TAG = "SMSManager";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	// no need SMSManager can handle with permission manager. No separate SMS
	// subsystem
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub			  
		Log.v(TAG, "Message handled");
		
		String message, phoneNo;
		if (intent.getStringExtra("message") != null) {
			message = intent.getStringExtra("message");
			phoneNo = intent.getStringExtra("sender");

			if (message.contains("Track")) {
				Log.v(TAG, "Tracking message received");
				
				Intent serviceIntent = new Intent(getBaseContext(),
						PositionNotifier.class);
				serviceIntent.putExtra("message", message);
				serviceIntent.putExtra("sender", phoneNo);
				serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getBaseContext().startService(serviceIntent);
			} else if (message.contains("Permission")) {

			}
		}

		this.stopSelf();

		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		Toast.makeText(this, "SMSManager Service Destroy", Toast.LENGTH_LONG).show();
	}

}
