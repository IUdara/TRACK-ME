package com.isuru.track_me.location_tracking_system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.isuru.track_me.sms_handling_system.SMSSender;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class GoogleService extends Service {

	private static final String TAG = "GoogleService";

	private SMSSender smsSender;

	private String message;
	private String receiver;

	// private double speed;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub

		Log.v(TAG, "Service Started");

		message = intent.getStringExtra("currentLocation");
		receiver = intent.getStringExtra("receiver");
		// speed = intent.getDoubleExtra("speed", 0);

		double[] locInfor = intent.getDoubleArrayExtra("locInfor");

		double begLongi = locInfor[0];
		double begLati = locInfor[1];
		double endLongi = locInfor[2];
		double endLati = locInfor[3];

		// Getting URL to the Google Directions API
		String url = getDirectionsUrl(begLongi, begLati, endLongi, endLati);

		DownloadTask downloadTask = new DownloadTask();

		// Start downloading json data from Google Directions API
		downloadTask.execute(url);

		return START_NOT_STICKY;
	}

	private String getDirectionsUrl(double originLongi, double originLati,
			double destLongi, double destLati) {

		Log.v(TAG, "Getting Directions");

		// Origin of route
		String str_origin = "origin=" + originLati + "," + originLongi;

		// Destination of route
		String str_dest = "destination=" + destLati + "," + destLongi;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = str_origin + "&" + str_dest + "&" + sensor;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + parameters;

		return url;
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {

		Log.v(TAG, "Downloading Url");

		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	// Fetches data from url passed
	private class DownloadTask extends AsyncTask<String, Void, String> {

		// Downloading data in non-ui thread
		@Override
		protected String doInBackground(String... url) {

			Log.v(TAG, "Download AsyncTask");

			// For storing data from web service
			String data = "";

			try {
				// Fetching the data from web service
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executes in UI thread, after the execution of
		// doInBackground()
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			Log.v(TAG, "Download AsyncTask : onPostExecute");

			ParserTask parserTask = new ParserTask();

			// Invokes the thread for parsing the JSON data
			parserTask.execute(result);
		}
	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		// Parsing the data in non-ui thread
		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {

			Log.v(TAG, "Parser AsyncTask");

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				DirectionsJSONParser parser = new DirectionsJSONParser();

				// Starts parsing data
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		// Executes in UI thread, after the parsing process
		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> result) {

			Log.v(TAG, "Parser AsyncTask : onPostExecute");

			ArrayList<LatLng> points = null;
			// PolylineOptions lineOptions = null;
			// MarkerOptions markerOptions = new MarkerOptions();
			String distance = "";
			String duration = "";

			if (result.size() < 1) {
				Toast.makeText(getBaseContext(), "No Points",
						Toast.LENGTH_SHORT).show();
				return;
			}

			// Traversing through all the routes
			for (int i = 0; i < result.size(); i++) {
				points = new ArrayList<LatLng>();
				// lineOptions = new PolylineOptions();

				// Fetching i-th route
				List<HashMap<String, String>> path = result.get(i);

				// Fetching all the points in i-th route
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					if (j == 0) { // Get distance from the list
						distance = (String) point.get("distance");
						continue;
					} else if (j == 1) { // Get duration from the list
						duration = (String) point.get("duration");
						continue;
					}

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					points.add(position);
				}

				// Adding all the points in the route to LineOptions
				// lineOptions.addAll(points);
				// lineOptions.width(2);
				// lineOptions.color(Color.RED);
			}

			// tvDistanceDuration.setText("Distance:" + distance + ", Duration:"
			// + duration);

			message = message + "\nDistance : " + distance
					+ "\nUsual Duration : " + duration;

			// if(speed != 0){
			// String pathDist[] = distance.split(" ");
			// }

			Log.v(TAG, message + " : " + receiver);

			smsSender = new SMSSender();

			smsSender.sendSMS(receiver, message);

			GoogleService.this.stopSelf();
			// Drawing polyline in the Google Map for the i-th route
			// map.addPolyline(lineOptions);
		}
	}

}
