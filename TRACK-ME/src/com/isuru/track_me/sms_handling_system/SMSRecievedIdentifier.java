package com.isuru.track_me.sms_handling_system;

import com.isuru.track_me.permission_handling_system.PermissionManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSRecievedIdentifier extends BroadcastReceiver {

	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private static final String TAG = "SMSBroadcastReceiver";

	String senderInfo, messageBody;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Intent recieved: " + intent.getAction());

		if (intent.getAction().equals(SMS_RECEIVED)) {
			Log.i(TAG, "Intent recieved: inside first if");
			senderInfo = "";
			messageBody = "";

			Bundle bundle = intent.getExtras();
			// Intent initialized to PermissionManager class
			Intent serviceIntent = new Intent(context, PermissionManager.class);

			if (bundle != null) {
				Object[] pdus = (Object[]) bundle.get("pdus");
				final SmsMessage[] messages = new SmsMessage[pdus.length];
				for (int i = 0; i < pdus.length; i++) {
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					senderInfo += messages[i].getOriginatingAddress(); // Extract
																		// sender
																		// phone
																		// no
					messageBody += messages[i].getMessageBody().toString(); // Extract
																			// SMS
																			// message
				}
				if (messages.length > -1) {
					Log.i(TAG, "Message recieved: " + messageBody + " from "
							+ senderInfo);
					// Data to be sent to PermissionManager class
					serviceIntent.putExtra("sender", senderInfo);
					serviceIntent.putExtra("message", messageBody);
					serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startService(serviceIntent);
				}
			}
		}
	}

}
