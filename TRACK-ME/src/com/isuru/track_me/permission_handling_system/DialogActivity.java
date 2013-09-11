package com.isuru.track_me.permission_handling_system;

import com.isuru.track_me.R;
import com.isuru.track_me.permission_handling_system.PermissionManager.LocalBinder;
import com.isuru.track_me.sms_handling_system.SMSSender;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.widget.Toast;

public class DialogActivity extends Activity {

	private String message;
	private boolean mBounded;
	private PermissionManager permissionManager;
	private static final String TAG = "DialogActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authenticator);
		mBounded = false;
		Log.v(TAG, "Creating");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dialog, menu);
		return true;
	}

	public void showDialog() {
		Log.v(TAG, "Trying to get message");
		if (permissionManager != null) {
			message = permissionManager.getPromptingMessage();
			Log.v(TAG, "Got message " + message);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(this, android.R.style.Theme_Dialog));
		if (message != null)
			builder.setTitle("TRACK-ME Permission Request");
		builder.setMessage(message);
		builder.setPositiveButton("Accept",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						DialogActivity.this.requestAccepted();
						finish();
					}
				}).setNegativeButton("Reject",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						finish();
					}
				});
		builder.show();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.v(TAG, "Starting");
		Intent mIntent = new Intent(this, PermissionManager.class);
		bindService(mIntent, mConnection, BIND_AUTO_CREATE);
		Log.v(TAG, "Bound");
	}

	ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
			Log.v(TAG, "Service is disconnected");
			Toast.makeText(DialogActivity.this, "Service is disconnected",
					Toast.LENGTH_LONG).show();
			mBounded = false;
			permissionManager = null;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.v(TAG, "Service is connected");
			Toast.makeText(DialogActivity.this, "Service is connected",
					Toast.LENGTH_LONG).show();
			mBounded = true;
			LocalBinder mLocalBinder = (LocalBinder) service;
			permissionManager = mLocalBinder.getServerInstance();
			message = permissionManager.getPromptingMessage();
			Log.v(TAG, "Got message " + message);
			DialogActivity.this.showDialog();
		}

	};

	@Override
	protected void onStop() {
		super.onStop();
		if (mBounded) {
			permissionManager.stopSelf();
			unbindService(mConnection);
			mBounded = false;
		}
	}

	public void requestAccepted() {
		SMSSender smsSender = new SMSSender();
		DBhandler permissionHandler = new DBhandler(this);
		RandomGenerator randomGenerator = new RandomGenerator(5);
		String requestor = permissionManager.getPhoneNo();
		String permissionCode = randomGenerator.nextString();
		permissionManager.setPermissionCode(permissionCode);
		permissionHandler.open();
		permissionHandler.makepermissionEntry(permissionManager.getPermission());
		permissionHandler.close();
		String reply = "Permission Accepted. Send 'Track: " + permissionCode
				+ "' to track.";
		smsSender.sendSMS(requestor, reply);
	}

}
