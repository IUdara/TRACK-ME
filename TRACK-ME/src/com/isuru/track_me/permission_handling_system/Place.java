package com.isuru.track_me.permission_handling_system;

public class Place {
	
	private String longitude;
	private String latitude;
	private String city;
	
	public Place(String longi, String lati, String name) {
		longitude = longi;
		latitude = lati;
		city = name;
	}
	
	public Place(String longi, String lati) {
		longitude = longi;
		latitude = lati;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}	

}
