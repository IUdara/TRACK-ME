package com.isuru.track_me.location_tracking_system;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class GPSManager implements LocationListener {
	
//	private static PositionManager gir;
	private EditText editLocation = null;	
	private ProgressBar pb =null;
	private Context cntxt;
	private static final String TAG = "Debug";

	GPSManager(Context context) {
		cntxt = context;
	}

//	public static PositionManager getGIR() {
//		if (gir == null) {
//			gir = new PositionManager();
//		}
//		return gir;
//	}

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub        	
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub        	
    }
//	
//
//	public void getCurrentLocation() {
//
//	}
//
//	public void getDistance() {
//
//	}
//
//	public void getTime() {
//
//	}

	@Override
	public void onLocationChanged(Location loc) {
		// TODO Auto-generated method stub
		editLocation.setText("");
    	pb.setVisibility(View.INVISIBLE);
        Toast.makeText(cntxt,"Location changed : Lat: " + loc.getLatitude()
                        + " Lng: " + loc.getLongitude(),Toast.LENGTH_SHORT).show();
        String longitude = "Longitude: " +loc.getLongitude();  
		Log.v(TAG, longitude);
	    String latitude = "Latitude: " +loc.getLatitude();
	    Log.v(TAG, latitude);
	    
	    /*----------to get City-Name from coordinates ------------- */
	    String cityName=null;      		      
	    Geocoder gcd = new Geocoder(cntxt, Locale.getDefault());      		     
	    List<Address>  addresses;  
	    try {  
	     addresses = gcd.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);  
	     if (addresses.size() > 0)  
	      System.out.println(addresses.get(0).getLocality());  
	     cityName=addresses.get(0).getLocality();  
	    } catch (IOException e) {    		      
	     e.printStackTrace();  
	    } 
	    
	    String s = longitude+"\n"+latitude +"\n\nMy Currrent City is: "+cityName;
		    editLocation.setText(s);
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}
