package com.isuru.track_me.authentication_system;

/**
 * @Author : Isuru Jayaweera
 * @email  : jayaweera.10@cse.mrt.ac.lk
 */

import com.isuru.track_me.R;
import com.isuru.track_me.TrackMe;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well. A user needs to be singed up at the first launch of this activity and
 * after singed up he only can sign in
 */

public class Authenticator extends Activity {

	/**
	 * The default email to populate the email field with.
	 */
	// public static final String EXTRA_EMAIL =
	// "com.example.android.authenticatordemo.extra.EMAIL";

	// private static final String TAG = "Authenticator";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	private String uNamePass;

	// Values for email and password at the time of the login attempt.
	private String mUsername;
	private String mPassword;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_authenticator);

		// Set up the login form.

		// mUsername = getIntent().getStringExtra(EXTRA_EMAIL);
		mUsernameView = (EditText) findViewById(R.id.username);
		mUsernameView.setText(mUsername);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.authenticator, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 5) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid user name.
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		} else if (mUsername.length() < 5) {
			mUsernameView.setError(getString(R.string.error_invalid_username));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.

			SharedPreferences sPrefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			String aNamePass = sPrefs.getString("AuthData", "Empty");

			if (aNamePass.equals("Empty")) {
				Authenticator.this.showDialog(mPassword);
			} else {
				mLoginStatusMessageView
						.setText(R.string.login_progress_signing_in);
				showProgress(true);
				mAuthTask = new UserLoginTask();
				mAuthTask.execute((Void) null);
			}
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {

			// Log.v(TAG, "Authenticating");

			SharedPreferences sPref = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			String encNamePass = sPref.getString("AuthData", "Empty");

			// Log.v(TAG, "Access Encrypted Password");

			if (encNamePass.equals("Empty")) {
				// Log.v(TAG, "1) First Time");
				// if (Authenticator.this.showDialog(mPassword)) {

				uNamePass = mUsername.concat(":" + mPassword);
				// Log.v(TAG, "2) First Time");
				try {
					// Log.v(TAG, "3) First Time");
					encNamePass = SimpleCrypto.encrypt("gajk@390#6gf",
							uNamePass);
					// Log.v(TAG, "4) First Time");
					SharedPreferences sharedPreferences = PreferenceManager
							.getDefaultSharedPreferences(getApplicationContext());
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString("AuthData", encNamePass);
					editor.putString("UserName", mUsername);
					editor.commit();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				// } else {
				// return false;
				// }
			} else {
				// Log.v(TAG, "Not First Time");

				try {
					uNamePass = SimpleCrypto.decrypt("gajk@390#6gf",
							encNamePass);
					String[] pieces = uNamePass.split(":");
					if (pieces[0].equals(mUsername)) {
						// Account exists, return true if the password matches.
						return pieces[1].equals(mPassword);
					}
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}

			// Log.v(TAG, "Exiting");

			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				Intent intent = new Intent(getBaseContext(), TrackMe.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getBaseContext().startActivity(intent);
				finish();
			} else {
				mUsernameView.setError(getString(R.string.error_invalid_email));
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

	private void showDialog(final String password) {

		LayoutInflater factory = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View textEntryView = factory.inflate(R.layout.confirm_dialog,
				null);
		AlertDialog.Builder alert = new AlertDialog.Builder(
				new ContextThemeWrapper(this, android.R.style.Theme_Dialog));

		alert.setTitle("Confirm Password");
		// Set an EditText view to get user input
		alert.setView(textEntryView);

		final EditText confirmPwd = (EditText) textEntryView
				.findViewById(R.id.etConf);

		alert.setPositiveButton("Confirm",
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
						View focusView = null;
						String confirmation = confirmPwd.getText().toString();

						if (confirmation.length() == 0) {
							confirmPwd
									.setError(getString(R.string.error_field_required));
							focusView = confirmPwd;
						} else if (!confirmation.equals(password)) {

							confirmPwd
									.setError(getString(R.string.error_mismatch_password2));
							focusView = confirmPwd;
						} else {

							closeDialog = true;

							mLoginStatusMessageView
									.setText(R.string.login_progress_signing_in);
							showProgress(true);
							mAuthTask = new UserLoginTask();
							mAuthTask.execute((Void) null);
							// Log.v(TAG, "Null Password");
						}

						if (!closeDialog) {
							focusView.requestFocus();
						}

						if (closeDialog) {
							pwdChanger.dismiss();
						}
					}
				});
	}
}
