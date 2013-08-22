package com.isuru.track_me.sms_handling_system;

import android.telephony.SmsMessage;

public class SMSReciever {
	String senderInfo, messageBody;
	Boolean isTracking, isPermision;

	public SMSReciever() {
		// TODO Auto-generated constructor stub
		senderInfo = null;
		messageBody = null;
		isTracking = false;
		isPermision = false;
	}

	public void checkSMS(SmsMessage[] message) {
		for (int i=0; i<message.length; i++){               
            senderInfo += message[i].getOriginatingAddress();
            messageBody += message[i].getMessageBody().toString();
        }
		
		if(messageBody.startsWith("Track", 0)){
			isTracking = true;
		}else if(messageBody.startsWith("Permission", 0)){
			isPermision = true;
		}
	}
	
	public boolean getIsTracking() {
		return isTracking;
	}
	
	public boolean getIsPermision() {
		return isPermision;
	}
}
