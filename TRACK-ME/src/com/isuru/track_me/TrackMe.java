package com.isuru.track_me;

/**
 * @Author : Isuru Jayaweera
 * @email  : jayaweera.10@cse.mrt.ac.lk
 */

import java.util.ArrayList;

import com.isuru.track_me.R;
import com.isuru.track_me.permission_handling_system.DBhandler;
import com.isuru.track_me.sms_handling_system.SMSSender;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TrackMe extends Activity {

	private static final String TAG = "TrackMe";
	private ListView permList;
	private ArrayList<Integer> permissionIndex;
	private ArrayList<String> permissionList;
	private ArrayAdapter<String> listAdapter;
	private SMSSender aSMSSender;
	private String phoneNo;

	// private boolean isShort;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		aSMSSender = new SMSSender();

		permList = (ListView) findViewById(R.id.lV);
		permissionList = new ArrayList<String>();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (listAdapter != null) {
			listAdapter.clear();
		}

		listAdapter = new ArrayAdapter<String>(this, R.layout.list_item,
				this.generateList());

		permList.setAdapter(listAdapter);

		registerForContextMenu(permList);

		listAdapter.notifyDataSetChanged();

		permList.refreshDrawableState();
		permList.requestLayout();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == permList.getId()) {

			Log.v(TAG, "Creating Context Menu");
			menu.setHeaderTitle("Manage Permission");
			getMenuInflater().inflate(R.menu.context_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterView.AdapterContextMenuInfo option = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();

		String message = permList.getItemAtPosition(option.position).toString();

		switch (item.getItemId()) {
		case R.id.iView:
			this.showDialog("Permission Details", message);
			break;
		case R.id.iEdit:

			break;
		case R.id.iRemove:
			this.deletePermission(message);
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	// Generate list of items to view in ListView
	private ArrayList<String> generateList() {
		DBhandler permissionHandler = new DBhandler(this);
		permissionHandler.open();
		if (permissionHandler.getData() != null) {
			permissionIndex = new ArrayList<Integer>();
			for (String s : permissionHandler.getData()) {
				String[] eachPerm = s.split(" ");
				phoneNo = eachPerm[1];
				String contName = this.getContactName(getApplicationContext(),
						eachPerm[1]);
				String dutation = "\nFrom " + eachPerm[2] + " " + eachPerm[3]
						+ " to " + eachPerm[4] + " " + eachPerm[5];
				Log.v(TAG, eachPerm[0] + ") To: " + contName + dutation);
				permissionList.add("To: " + contName + dutation);
				permissionIndex.add(Integer.parseInt(eachPerm[0]));
			}
			permissionHandler.close();
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

	private void showDialog(String title, String body) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(this, android.R.style.Theme_Dialog));

		if (body != null) {
			builder.setTitle(title);
			builder.setMessage(body);
			builder.setPositiveButton("Back",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			builder.show();
		}

	}

	// Deleting a tracking permission from database
	private void deletePermission(String permission) {

		if (permissionList.contains(permission)) {
			DBhandler permissionHandler = new DBhandler(this);
			permissionHandler.open();

			int index = permissionList.indexOf(permission);
			int dbID = permissionIndex.get(index);

			try {
				permissionHandler.deleteEntry(dbID);
				permissionList.remove(permission);
				permissionIndex.remove(index);

				// Inform the requester of permission
				aSMSSender
						.sendSMS(phoneNo,
								"Tracking permission which had been granted to you has been removed.");
			} catch (Exception e) {
				Log.v(TAG, "No permission item to delete");
			}
			permissionHandler.close();
		}
		listAdapter.notifyDataSetChanged();

	}

}
