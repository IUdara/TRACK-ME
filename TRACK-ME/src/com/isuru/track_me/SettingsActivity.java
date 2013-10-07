package com.isuru.track_me;

import com.isuru.track_me.authentication_system.SimpleCrypto;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

	private static final String TAG = "SettingsActivity";

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);

		// Get the custom preference
		Preference customPref = (Preference) findPreference("prefPwd");

		customPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {

						// [ NEED TO CALL DIALOG FROM HERE ]
						SettingsActivity.this.showDialog();
						return false;
					}
				});

	}

	private void showDialog() {

		LayoutInflater factory = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View textEntryView = factory.inflate(R.layout.pwd_dialog, null);
		AlertDialog.Builder alert = new AlertDialog.Builder(
				new ContextThemeWrapper(this, android.R.style.Theme_Dialog));

		alert.setTitle("Change Password");
		// Set an EditText view to get user input
		alert.setView(textEntryView);

		// final TextView currentTV = (TextView) textEntryView
		// .findViewById(R.id.tvCurrent);
		// final TextView newTV = (TextView) textEntryView
		// .findViewById(R.id.tvNew);
		// final TextView confirmTV = (TextView) textEntryView
		// .findViewById(R.id.tvConfirm);

		final EditText current = (EditText) textEntryView
				.findViewById(R.id.etCurrent);
		final EditText newPwd = (EditText) textEntryView
				.findViewById(R.id.etNew);
		final EditText confirmPwd = (EditText) textEntryView
				.findViewById(R.id.etConfirm);

		alert.setPositiveButton("Change",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});

		final AlertDialog pwdChanger = alert.create();
		pwdChanger.show();

		pwdChanger.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						Boolean closeDialog = false;

						SharedPreferences sPref = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						String encNamePass = sPref.getString("AuthData",
								"Empty");
						String userName = sPref.getString("UserName", "Empty");

						Log.v(TAG, "Trying to change : " + encNamePass + " "
								+ userName);

						if (!encNamePass.equals("Empty")
								&& !userName.equals("Empty")) {

							String currentPwd = current.getText().toString();
							String authData = userName.concat(":" + currentPwd);
							String encCurrent;
							try {
								encCurrent = SimpleCrypto.encrypt(
										"gajk@390#6gf", authData);
								View focusView = null;

								if (encCurrent.equals(encNamePass)) {
									if (newPwd.getText().length() == 0) {
										newPwd.setError(getString(R.string.error_field_required));
										focusView = newPwd;
									} else if (newPwd.getText().length() < 5) {
										newPwd.setError(getString(R.string.error_invalid_password));
										focusView = newPwd;
									} else if (confirmPwd.getText().length() == 0) {
										confirmPwd
												.setError(getString(R.string.error_field_required));
										focusView = confirmPwd;
									} else if (!newPwd
											.getText()
											.toString()
											.equals(confirmPwd.getText()
													.toString())) {
										confirmPwd
												.setError(getString(R.string.error_mismatch_password2));
										focusView = confirmPwd;
									} else {
										String newPassword = newPwd.getText()
												.toString();
										String newAuthData = userName
												.concat(":" + newPassword);
										String encNew = SimpleCrypto.encrypt(
												"gajk@390#6gf", newAuthData);
										sPref.edit()
												.putString("AuthData", encNew)
												.commit();

										Toast.makeText(getApplicationContext(),
												"Password Changed",
												Toast.LENGTH_SHORT).show();
										closeDialog = true;
									}
								} else {
									current.setError(getString(R.string.error_mismatch_password1));
									focusView = current;

									Log.v(TAG, "Null Password");
								}

								if (!closeDialog) {
									focusView.requestFocus();
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							Toast.makeText(getApplicationContext(),
									"Password Not Changed", Toast.LENGTH_SHORT)
									.show();
						}
						if (closeDialog)
							pwdChanger.dismiss();
						// else dialog stays open. Make sure you have an obvious
						// way to close the dialog especially if you set
						// cancellable to false.
					}
				});
	}

}
