package com.isuru.track_me.permission_handling_system;

/**
 * @Author : Isuru Jayaweera
 * @email  : jayaweera.10@cse.mrt.ac.lk
 */

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBhandler {
	private static final String TAG = "DBhandler";

	private static final String DATABASE_NAME = "PermissionDB";
	private static final String TABLE_NAME = "permission_table";
	private static final int DATABASE_VERSION = 1;

	public static final String KEY_ROWID = "_id";
	public static final String KEY_PERMKEY = "permission_code";
	public static final String KEY_PHONE = "phone_number";
	public static final String KEY_BEGT = "begin_time";
	public static final String KEY_ENDT = "end_time";
	public static final String KEY_DEST = "destination";
	public static final String KEY_PERIOD = "period";

	private DBHelper permissionDBHelper;
	private final Context permissionContext;
	private SQLiteDatabase permissionSQLDB;

	// SQLiteOpenHelper checks whether the DB exist, otherwise create it with
	// onCreate() and make sure there is only one DB is created. Maintain DB.
	// But getting and putting data are not handled through it.
	private static class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			// TODO Auto-generated constructor stub
		}

		// When Database is created for the first time
		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + KEY_ROWID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_PERMKEY
					+ " CHAR(5) NOT NULL, " + KEY_PHONE
					+ " VARCHAR(12) NOT NULL, " + KEY_BEGT
					+ " DATETIME NOT NULL, " + KEY_ENDT
					+ " DATETIME NOT NULL, " + KEY_DEST + " TEXT," + KEY_PERIOD
					+ " TIME);");
		}

		// Called when schema of DB need to be upgraded
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
			onCreate(db);
		}

	}

	public DBhandler(Context c) {
		permissionContext = c;
	}

	public DBhandler open() throws SQLException {
		if (permissionDBHelper == null) {
			permissionDBHelper = new DBHelper(permissionContext);
			permissionSQLDB = permissionDBHelper.getWritableDatabase();
		}
		return this;
	}

	public void close() {
		if (permissionSQLDB != null) {
			permissionSQLDB.close();
		}
		permissionDBHelper.close();
	}

	public long makepermissionEntry(Permission permission) throws SQLException {
		// TODO Auto-generated method stub
		DateTimeFormatter dtFormat = DateTimeFormat
				.forPattern("yyyy-MM-dd HH:mm");
		ContentValues tableData = new ContentValues();

		// this.updateIDs();

		tableData.put(KEY_PERMKEY, permission.getPermissionCode());
		tableData.put(KEY_PHONE, permission.getOwner());
		tableData.put(KEY_BEGT,
				permission.getPermissionStart().toString(dtFormat));
		tableData.put(KEY_ENDT, permission.getPermissionEnd()
				.toString(dtFormat));
		// tableData.put(KEY_DEST, permission.getIsDestinated());
		// tableData.put(KEY_PERIOD, permission.getUpdatePeriod().getMinutes());
		return permissionSQLDB.insert(TABLE_NAME, null, tableData);
	}

	public ArrayList<String> getData() {
		// this.updateIDs();
		String[] tabCols = new String[] { KEY_ROWID, KEY_PERMKEY, KEY_PHONE,
				KEY_BEGT, KEY_ENDT };
		Cursor dbPos = permissionSQLDB.query(TABLE_NAME, tabCols, null, null,
				null, null, null);
		ArrayList<String> permissionList = new ArrayList<String>();
		try {
			int iRow = dbPos.getColumnIndex(KEY_ROWID);
			// int iPermCode = dbPos.getColumnIndex(KEY_PERMKEY);
			int iPone = dbPos.getColumnIndex(KEY_PHONE);
			int iBeginT = dbPos.getColumnIndex(KEY_BEGT);
			int iEndT = dbPos.getColumnIndex(KEY_ENDT);

			for (dbPos.moveToFirst(); !dbPos.isAfterLast(); dbPos.moveToNext()) {
				String resultData = "";
				resultData = resultData + dbPos.getString(iRow) + " "
						+ dbPos.getString(iPone) + " "
						+ dbPos.getString(iBeginT) + " "
						+ dbPos.getString(iEndT);
				permissionList.add(resultData);
			}
		}

		catch (Exception e) {
			Log.v(TAG, e.toString());
		}

		finally {
			if (dbPos != null) {
				dbPos.close();
			}
		}

		return permissionList;
	}

	public String[] getPermissionData(String permCode, String phoneNo)
			throws SQLException {
		String[] tabName = new String[] { KEY_BEGT, KEY_ENDT };

		Cursor dbPos = permissionSQLDB.query(TABLE_NAME, tabName, KEY_PERMKEY
				+ "=" + permCode + " AND " + KEY_PHONE + "=" + phoneNo, null,
				null, null, null, null);

		if (dbPos != null) {
			dbPos.moveToFirst();
			String[] resultName = {
					dbPos.getString(dbPos.getColumnIndex(KEY_BEGT)),
					dbPos.getString(dbPos.getColumnIndex(KEY_ENDT)) };
			dbPos.close();
			return resultName;
		}

		return null;
	}

	public Boolean checkTrackingValidity(String permCode, String phoneNo,
			DateTime currentT) throws SQLException {
		// TODO Auto-generated method stub
		String[] tabName = new String[] { KEY_BEGT, KEY_ENDT };
		DateTime start, end;
		Interval interval;
		Boolean canTrack = false;

		DateTimeFormatter dtFormat = DateTimeFormat
				.forPattern("yyyy-MM-dd HH:mm");

		// String mocking = "2013-09-11 13:00";
		// DateTime mockDT = DateTime.parse(mocking, dtFormat);
		// Log.v(TAG, "Mock Date " + mockDT.toString(dtFormat));

		Cursor dbPos = permissionSQLDB.query(TABLE_NAME, tabName, KEY_PERMKEY
				+ "=? AND " + KEY_PHONE + "=?", new String[] { permCode,
				phoneNo }, null, null, null);

		for (dbPos.moveToFirst(); !dbPos.isAfterLast(); dbPos.moveToNext()) {
			Log.v(TAG, "Entered Loop ");
			start = DateTime.parse(
					dbPos.getString(dbPos.getColumnIndex(KEY_BEGT)), dtFormat);
			Log.v(TAG, "Start Date " + start.toString(dtFormat));
			end = DateTime.parse(
					dbPos.getString(dbPos.getColumnIndex(KEY_ENDT)), dtFormat);
			Log.v(TAG, "End Date " + end.toString(dtFormat));
			interval = new Interval(start, end);
			if (interval.contains(currentT)) {
				canTrack = true;
			}
		}

		dbPos.close();

		return canTrack;
	}

	/*
	 * To be completed public void editEntry(int rowEditID, String editName,
	 * String editRate) throws SQLException { Cursor dbPos =
	 * permissionSQLDB.query(TABLE_NAME, null, KEY_ROWID + "=" + rowEditID,
	 * null, null, null, null, null); if (dbPos != null && dbPos.getCount() > 0)
	 * { ContentValues tableData = new ContentValues(); tableData.put(KEY_PHONE,
	 * editName); tableData.put(KEY_BEGT, editRate);
	 * permissionSQLDB.update(TABLE_NAME, tableData, KEY_ROWID + "=" +
	 * rowEditID, null); } else { throw new NullPointerException(); }
	 * 
	 * }
	 */

	public void deleteEntry(int rowDeleteID) throws SQLException {
		// TODO Auto-generated method stub
		Cursor dbPos = permissionSQLDB.query(TABLE_NAME, null, KEY_ROWID + "="
				+ rowDeleteID, null, null, null, null, null);
		if (dbPos != null && dbPos.getCount() > 0) {
			permissionSQLDB.delete(TABLE_NAME, KEY_ROWID + "=" + rowDeleteID,
					null);
		} else {
			throw new NullPointerException();
		}
		// this.updateIDs();
		dbPos.close();
	}

	// private void updateIDs() {
	// Log.v(TAG, "Resetting Sequence ");
	// permissionSQLDB
	// .execSQL("UPDATE SQLITE_SEQUENCE SET seq = 0 WHERE name = '"
	// + TABLE_NAME + "';");
	// }
}
