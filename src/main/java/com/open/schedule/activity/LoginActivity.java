package com.open.schedule.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.open.schedule.R;
import com.open.schedule.io.packet.server.LoggedPacket;

public class LoginActivity extends ScheduleActivity implements UiMessageHandler {
	public static final int REGISTER = 1;

	public static final int RESULT_REGISTERED = RESULT_FIRST_USER + 1;

	private static final int PASSWORD_MIN_LENGTH = 4;

	private UserLoginTask authTask = null;

	// Values for email and password at the time of the login attempt.
	private String email = "l@m.c";
	private String password = "1111";

	private EditText emailView;
	private EditText passwordView;
	private View loginFormView;
	private View loginStatusView;
	private TextView loginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		emailView = (EditText) findViewById(R.id.email);
		emailView.setText(email);

		passwordView = (EditText) findViewById(R.id.password);
		passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}

				return false;
			}
		});

		loginFormView = findViewById(R.id.login_form);
		loginStatusView = findViewById(R.id.login_status);
		loginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.button_sign_in).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LoginActivity.this.attemptLogin();
			}
		});

		findViewById(R.id.button_register).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LoginActivity.this.openRegisterActivity();
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_REGISTERED)
			this.finish();
	}

	@Override
	public void handleMessage(Message message) {
		UiMessageType type = UiMessageType.values()[message.what];
		switch (type) {
			case UI_MESSAGE_LOGGED: {
				LoggedPacket.Status status = (LoggedPacket.Status) message.obj;
				switch (status) {
					case SUCCESS:
						this.loginSucceeded();
						break;
					case FAILURE:
						this.loginFailed();
						break;
				}
				return;
			}
			default:
				throw new IllegalArgumentException("Wrong message  type" + type);
		}

	}

	private void openRegisterActivity() {
		Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
		this.startActivityForResult(registerIntent, REGISTER);
	}

	private void attemptLogin() {
		if (authTask != null) {
			return;
		}

		boolean isValuesCorrected = this.checkValues();
		if (isValuesCorrected) {
			this.loginStatusMessageView.setText(R.string.login_progress_signing_in);
			this.showProgress(true);
			new UserLoginTask().execute();
		}
	}

	private boolean checkValues() {
		this.emailView.setError(null);
		this.passwordView.setError(null);

		this.email = this.emailView.getText().toString();
		this.password = this.passwordView.getText().toString();

		if (TextUtils.isEmpty(this.password)) {
			this.passwordView.setError(getString(R.string.login_error_field_required));
			this.passwordView.requestFocus();
			return false;
		} else if (this.password.length() < PASSWORD_MIN_LENGTH) {
			this.passwordView.setError(getString(R.string.login_error_invalid_password));
			this.passwordView.requestFocus();
			return false;
		}

		if (TextUtils.isEmpty(this.email)) {
			this.emailView.setError(getString(R.string.login_error_field_required));
			this.emailView.requestFocus();
			return false;
		} else if (!email.contains("@")) {
			this.emailView.setError(getString(R.string.login_error_invalid_email));
			this.emailView.requestFocus();
			return false;
		}

		return true;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			this.loginStatusView.setVisibility(View.VISIBLE);
			this.loginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					LoginActivity.this.loginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});

			this.loginFormView.setVisibility(View.VISIBLE);
			this.loginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					LoginActivity.this.loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});
		} else {
			this.loginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			this.loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public void loginSucceeded() {
		this.finish();
	}

	public void loginFailed() {
		this.authTask = null;
		this.showProgress(false);

		String error = this.getString(R.string.login_error_invalid_auth);
		this.passwordView.setError(error);
		this.passwordView.requestFocus();
	}

	public class UserLoginTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			if (!LoginActivity.this.isConnected())
				return null;

			LoginActivity.this.getClient().login(LoginActivity.this.email, LoginActivity.this.password, LoginActivity.this);
			return null;
		}

		@Override
		protected void onCancelled() {
			LoginActivity.this.authTask = null;
			showProgress(false);
		}
	}
}
