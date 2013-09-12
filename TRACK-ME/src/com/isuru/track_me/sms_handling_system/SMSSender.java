package com.isuru.track_me.sms_handling_system;

import android.telephony.SmsManager;
import android.util.Log;

public class SMSSender {
	
	private static final String TAG = "SMSSender";

	public SMSSender() {
		// TODO Auto-generated constructor stub
	}

	public void sendSMS(String phoneNo, String message) {
		Log.v(TAG, "SMS sent");
		SmsManager manager = SmsManager.getDefault();
		manager.sendTextMessage(phoneNo, null, message, null, null);
	}
}
