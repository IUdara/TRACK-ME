package com.isuru.track_me.permission_handling_system;

/**
 * @Author : Isuru Jayaweera
 * @email  : jayaweera.10@cse.mrt.ac.lk
 */

import java.util.Calendar;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.isuru.track_me.location_tracking_system.SrvPositioning;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.widget.Toast;

public class PermissionManager extends Service {

	private Permission permission;
	private IBinder mBinder = new LocalBinder();
	private String promptingMessage, phoneNo;
	private static final String TAG = "PermissionManager";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v(TAG, "Message handled");

		promptingMessage = "It works";

		String message;
		if (intent.getStringExtra("message") != null) {
			message = intent.getStringExtra("message").trim();
			phoneNo = intent.getStringExtra("sender");

			if (message.startsWith("Track:")) { // a tracking request
				Log.v(TAG, "Tracking message received");
				message = message.replace("Track: ", "");

				String[] msg = message.split(" ");

				if (message.length() <= 7 || msg.length == 3) { // need to
																// modify, if
																// none to
																// sender
																// location
					Log.v(TAG, "Tracking message OK");

					boolean isPeriodic = false;
					String dest = "none"; // set to get location of sender

					if (message.length() == 7 || msg.length == 3) {
						if (msg[1].equals("R")) {
							isPeriodic = true;
							message = msg[0];
							if (msg.length == 3) {
								dest = msg[2];
								Log.v(TAG, "Destination : " + dest);
							}
						} else if (msg[1].equals("N")) {
							isPeriodic = false;
							message = msg[0];
							if (msg.length == 3) {
								dest = msg[2];
								Log.v(TAG, "Destination" + dest);
							}
						} else {
							this.stopSelf();
						}
					} else if (message.length() == 5) {
						isPeriodic = false;
					} else {
						this.stopSelf();
					}

					DateTime curentT = this.getCurrentDate(); // get current
																// DateTime

					DBhandler permissionHandler = new DBhandler(this);
					permissionHandler.open();
					// check whether requester has tracking permission
					Boolean isPermitted = permissionHandler
							.checkTrackingValidity(message, phoneNo, curentT);
					Log.v(TAG, "Code Checking : " + message);
					permissionHandler.close();

					if (isPermitted) { // when he has permission
						Log.v(TAG, "You can track");

						SharedPreferences sPref = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						SharedPreferences.Editor editor = sPref.edit();
						editor.putInt("Frequency", 3);
						editor.commit();

						// start PositionNotifier service
						Intent serviceIntent = new Intent(getBaseContext(),
								SrvPositioning.class);
						serviceIntent.putExtra("sender", phoneNo);
						serviceIntent.putExtra("periodic", isPeriodic);
						serviceIntent.putExtra("destination", dest);
						serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getBaseContext().startService(serviceIntent);
					} else {
						Log.v(TAG, "No permission to track");
					}
				} else {
					Log.v(TAG, "Wrong tracking message");
				}
				this.stopSelf();

				return Service.START_NOT_STICKY;

			} else if (message.startsWith("Permission:")) { // a permission
															// request
				Log.v(TAG, "Permission message received");
				message = message.replace("Permission: ", "");
				// extract requested permissions into a Permission object
				permission = extractPermission(message, phoneNo);
				Log.v(TAG, "Permission established");
				String requester = this.getContactName(getBaseContext(),
						phoneNo); // get requester's contact name from phone's
									// contacts
				Log.v(TAG, "Contact Received");
				// format requested permissions to display
				DateTimeFormatter format = DateTimeFormat
						.forPattern("yyyy-MM-dd hh:mm aa");
				promptingMessage = requester
						+ " requests for \ntracking permission \nfrom "
						+ permission.getPermissionStart().toString(format)
						+ " to "
						+ permission.getPermissionEnd().toString(format);
				Log.v(TAG, "Message Created");

				// ***** Notification prompts twice *****
				this.launchNotification(promptingMessage); // launching
															// notification
				Log.v(TAG, "Message Prompted");
			}
		}

		return Service.START_REDELIVER_INTENT;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public Permission getPermission() {
		return permission;
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		Toast.makeText(this, "PermissionManager Service Destroy",
				Toast.LENGTH_LONG).show();
	}

	// extract requested permissions into a Permission object
	private Permission extractPermission(String message, String sender) {
		String[] permissionList = message.split(" ");
		String[][] timeDetails = new String[4][3];
		timeDetails[0] = permissionList[0].split("-");
		timeDetails[1] = permissionList[1].split(":");
		timeDetails[2] = permissionList[2].split("-");
		timeDetails[3] = permissionList[3].split(":");

		int[][] timeDate = new int[4][3];

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				if (!((i == 1 || i == 3) && j == 2)) {
					timeDate[i][j] = Integer.parseInt(timeDetails[i][j]);
				}
			}
		}
		Permission permission = new Permission(timeDate, sender);

		return permission;
	}

	// return current DateTime
	private DateTime getCurrentDate() {
		Calendar calendar = Calendar.getInstance();
		DateTime currentT = new DateTime(calendar.getTime());
		return currentT;
	}

	// Launching the notification
	@SuppressWarnings("deprecation")
	private void launchNotification(String message) {
		Context context = getApplicationContext();

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		Intent notificationIntent = new Intent(this, DialogActivity.class);
		notificationIntent.setClass(context, DialogActivity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		Notification notification = new Notification(
				android.R.drawable.ic_dialog_alert, message,
				System.currentTimeMillis());

		String notificationTitle = "TRACK-ME Permission Request";

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(this, notificationTitle, message,
				contentIntent);
		notificationManager.notify(0, notification);
	}

	// Get contact name for a phone no from phone's contacts
	private String getContactName(Context context, String phoneNumber) {
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri,
				new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		if (cursor == null) {
			return phoneNumber;
		}
		String contactName = phoneNumber;
		if (cursor.moveToFirst()) {
			contactName = cursor.getString(cursor
					.getColumnIndex(PhoneLookup.DISPLAY_NAME));
		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}

		return contactName;
	}

	// Use for bind this service with DialogActivity
	public class LocalBinder extends Binder {
		public PermissionManager getServerInstance() {
			return PermissionManager.this;
		}
	}

	public String getPromptingMessage() {
		return promptingMessage;
	}

	public void setPermissionCode(String permissionCode) {
		permission.setPermissionCode(permissionCode);
		Log.v(TAG, "Permission Code Added");
	}

}
