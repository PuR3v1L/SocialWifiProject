package com.spydiko.socialwifi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {

	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;
	private String macAddress;

	// UI references.
	private SocialWifi socialWifi;
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private String hostIPstr = "83.212.121.161";
	private int serverPort = 44444;
	private static final String TAG = "LoginActivity";
	private CheckBox showPasswordCB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.
		mEmailView = (EditText) findViewById(R.id.email);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					return true;
				}
				return false;
			}
		});

		socialWifi = (SocialWifi) getApplication();
		macAddress = socialWifi.getWifi().getConnectionInfo().getMacAddress();
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin(true);
			}
		});
		findViewById(R.id.register_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin(false);
			}
		});

		showPasswordCB = (CheckBox) findViewById(R.id.show_password_login);
		showPasswordCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				EditText password = (EditText) findViewById(R.id.password);
				if (b) {
					password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				} else {
					password.setInputType(129);
				}
			}
		});
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */

	public void attemptLogin(boolean sign_in) {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 6) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (mEmail.length() < 6) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			if (sign_in) mAuthTask = new UserLoginTask(this, true);
			else mAuthTask = new UserLoginTask(this);
			mAuthTask.execute((Void) null);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		setResult(RESULT_CANCELED, new Intent());
		finish();
	}

	public void done() {
		socialWifi.setSharedPreferenceString("username", mEmail);
		setResult(RESULT_OK, new Intent());
		finish();
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
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate()
					.setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate()
					.setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

		private Context context;
		private Socket sk;
		private DataOutputStream dos;
		private DataInputStream dis;
		private String response;
		private boolean sign_in;

		public UserLoginTask(Context loginActivity) {
			this.context = loginActivity;
		}

		public UserLoginTask(Context loginActivity, boolean sign_in) {
			this.context = loginActivity;
			this.sign_in = sign_in;
		}

		public boolean sendCredentials(String action) {
			try {
				sk = new Socket();
				SocketAddress remoteaddr = new InetSocketAddress(hostIPstr, serverPort);
				sk.setSoTimeout(5000);
				sk.connect(remoteaddr, 5000);
				Log.d(TAG, "Socket opened");
				dos = new DataOutputStream(sk.getOutputStream());
				dis = new DataInputStream(sk.getInputStream());
				Log.d(TAG, "Trying to sent message");
				dos.writeBytes(action + "\r\n");
				dos.writeBytes(mEmail + "\r\n");
				dos.writeBytes(macAddress + "\r\n");
				dos.writeBytes(mPassword + "\r\n");
				response = dis.readLine();
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (response == null) {
				return false;
			} else if (response.equals("Done")) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (sign_in) return sendCredentials("login");
			else return sendCredentials("register");
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);
			Log.d(TAG, "Closing Everything");
			try {
				sk.close();
				dos.close();
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (success) {
				Toast.makeText(context, "OK!", Toast.LENGTH_SHORT).show();
				LoginActivity temp = (LoginActivity) context;
				temp.done();
				finish();
			} else {
				//	            mPasswordView.setError(getString(R.string.error_incorrect_password));
				mEmailView.setError(getString(R.string.error_incorrect_cred));
				mEmailView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
