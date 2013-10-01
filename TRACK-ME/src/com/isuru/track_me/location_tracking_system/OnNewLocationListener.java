package com.isuru.track_me.location_tracking_system;

import android.location.Location;

public interface OnNewLocationListener {
	public abstract void onNewLocationReceived(Location location);
}
