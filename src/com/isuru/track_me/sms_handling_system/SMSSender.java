package com.isuru.track_me.sms_handling_system;

import android.telephony.SmsManager;

public class SMSSender {
//	private static SMSSender ms;
//
//	private SMSSender() {
//
//	}
//
//	public static SMSSender getMS() {
//		if (ms == null) {
//			ms = new SMSSender();
//		}
//		return ms;
//	}

	public SMSSender() {
		// TODO Auto-generated constructor stub
	}

	public void sendSMS(String phoneNo, String message) {
		SmsManager manager = SmsManager.getDefault();
		manager.sendTextMessage(phoneNo, null, message, null, null);
	}
}
