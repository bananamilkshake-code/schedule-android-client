package com.open.schedule;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import events.listeners.EventListener;
import events.objects.Event;
import events.objects.EventWarehouse;
import io.Client;
import io.packet.server.LoginPacket;

public class LoginActivity extends Activity implements OnClickListener {
	public static final int REGISTER = 1;
	
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String email;
	private String password;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	
	private LoginActivityListener loginListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);

		// Set up the login form.
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(email);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
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

		findViewById(R.id.button_sign_in).setOnClickListener(this);
		findViewById(R.id.button_register).setOnClickListener(this);
		
		loginListener = new LoginActivityListener();
	}
	
	@Override
	protected void onStop() {
		loginListener.shutdown();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_sign_in:
			attemptLogin();
			break;
		case R.id.button_register:
			openRegisterActivity();
			break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (Client.getInstance().isLogged())
			finish();
	}

	private void openRegisterActivity() {
		Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
		startActivityForResult(registerIntent, REGISTER);
	}
	
	private void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		email = mEmailView.getText().toString();
		password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(password)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (password.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(email)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!email.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			focusView.requestFocus();
		} else {
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			new UserLoginTask().execute();
		}
	}

	private void setAuthorisationFail() {
		String error = getString(R.string.error_invalid_auth);
		mPasswordView.setError(error);
		mPasswordView.requestFocus();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});
		} else {
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	private class LoginActivityListener implements EventListener {
		public LoginActivityListener() {
			super();
			EventWarehouse.getInstance().addListener((EventListener)this, Event.Type.LOGIN);
		}

		public void handle(Event event) 
		{
			showProgress(false);
			switch (event.getType()) {
			case LOGIN: {
					LoginPacket.Status status = (LoginPacket.Status)event.getData();
					switch (status) {
					case SUCCESS:
						finish();
						break;
					case FAILURE:
						setAuthorisationFail();
						break;
					}
				}
				break;
			default:
				break;
			}
			
		}

		public final void shutdown() {
			EventWarehouse.getInstance().removeListener((EventListener)this, Event.Type.LOGIN);
		}
	}

	public class UserLoginTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			if (!Client.getInstance().isConnected())
				return null;
			Client.getInstance().login(email, password);
			return null;
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
