package com.isuru.track_me.location_tracking_system;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PositionNotifier extends Service{

	private PositionManager positionMangaer=null;

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
	}

	
}
