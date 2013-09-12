package com.isuru.track_me;

/**
 * @Author : Isuru Jayaweera
 * @email  : jayaweera.10@cse.mrt.ac.lk
 */

import java.util.ArrayList;

import com.isuru.track_me.R;
import com.isuru.track_me.permission_handling_system.DBhandler;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TrackMe extends Activity {

	ListView permList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		permList = (ListView) findViewById(R.id.lV);
		
		permList.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
				this.generateList()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Generate list of items to view in ListView
	private ArrayList<String> generateList() {
		DBhandler permissionHandler = new DBhandler(this);
		permissionHandler.open();
		if (permissionHandler.getData() != null) {
			ArrayList<String> permissionList = new ArrayList<String>();
			for (String s : permissionHandler.getData()) {
				String[] eachPerm = s.split(" ");
				String contName = this.getContactName(getApplicationContext(),
						eachPerm[0]);
				String dutation = "\nFrom " + eachPerm[1] + " " + eachPerm[2]
						+ " to " + eachPerm[3] + " " + eachPerm[4];
				permissionList.add("To: " + contName + dutation);
			}
			return permissionList;
		}
		permissionHandler.close();

		return null;
	}

	// Get contact name from the contacts of phone for a given phone number
	private String getContactName(Context context, String phoneNumber) {
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri,
				new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		if (cursor == null) {
			return phoneNumber;
		}
		String contactName = phoneNumber;
		if (cursor.moveToFirst()) {
			contactName = cursor.getString(cursor
					.getColumnIndex(PhoneLookup.DISPLAY_NAME));
			cursor.close();
		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}

		return contactName;
	}
}
