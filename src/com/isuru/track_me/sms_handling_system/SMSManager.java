package com.isuru.track_me.sms_handling_system;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;

public class SMSManager extends Service {

	SMSSender aSMSSender;
	SMSReciever aSMSReciever;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		aSMSSender = new SMSSender();
		aSMSReciever = new SMSReciever();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent.getBundleExtra("message") != null) {
			SmsMessage[] aSMS = this.extractMessage(intent);
			aSMSReciever.checkSMS(aSMS);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private SmsMessage[] extractMessage(Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			Object[] pdus = (Object[]) bundle.get("pdus");
			SmsMessage[] messages = new SmsMessage[pdus.length];
			for (int i = 0; i < pdus.length; i++) {
				messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			}
			return messages;
		} else {
			return null;
		}
	}

}
