package com.isuru.track_me.location_tracking_system;

public class PositionManager { //Turn on/off GPS radio automatically - Retrieve information
	private static PositionManager gir;

	private PositionManager() {

	}

	public static PositionManager getGIR() {
		if (gir == null) {
			gir = new PositionManager();
		}
		return gir;
	}

	public void getCurrentLocation() {

	}

	public void getDistance() {

	}

	public void getTime() {

	}
}
