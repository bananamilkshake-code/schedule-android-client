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
import com.open.schedule.io.packet.server.RegisteredPacket;

public class RegisterActivity extends ScheduleActivity implements UiMessageHandler {
	private RegisterTask registerTask = null;

	private String email;
	private String password;
	private String passwordVerification;
	private String name;

	private EditText emailView;
	private EditText passwordView;
	private EditText passwordVerifyView;
	private EditText nameView;
	private View loginFormView;
	private View loginStatusView;
	private TextView loginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);

		this.emailView = (EditText) findViewById(R.id.email);
		this.emailView.setText(this.email);

		this.passwordView = (EditText) findViewById(R.id.password);
		this.passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					register();
					return true;
				}
				return false;
			}
		});

		this.passwordVerifyView = (EditText) findViewById(R.id.password_verification);
		this.nameView = (EditText) findViewById(R.id.name);

		this.loginFormView = findViewById(R.id.login_form);
		this.loginStatusView = findViewById(R.id.login_status);
		this.loginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.register_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				register();
			}
		});
	}

	private void register() {
		if (this.registerTask != null) {
			return;
		}

		boolean isValuesCorrected = this.checkValues();
		if (isValuesCorrected) {
			this.loginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			this.registerTask = new RegisterTask();
			this.registerTask.execute();
		}
	}

	private boolean checkValues() {
		this.emailView.setError(null);
		this.passwordView.setError(null);
		this.passwordVerifyView.setError(null);
		this.nameView.setError(null);

		email = this.emailView.getText().toString();
		password = this.passwordView.getText().toString();
		passwordVerification = this.passwordVerifyView.getText().toString();
		name = this.nameView.getText().toString();

		if (TextUtils.isEmpty(password)) {
			this.passwordView.setError(getString(R.string.register_error_field_required));
			this.passwordView.requestFocus();
			return false;
		} else if (password.length() < 4) {
			this.passwordView.setError(getString(R.string.register_error_invalid_password));
			this.passwordView.requestFocus();
			return false;
		} else if (TextUtils.isEmpty(passwordVerification)) {
			this.passwordVerifyView.setError(getString(R.string.register_error_no_verification));
			this.passwordVerifyView.requestFocus();
			return false;
		} else if (!TextUtils.equals(password, passwordVerification)) {
			this.passwordVerifyView.getEditableText().clear();
			this.passwordView.setError(getString(R.string.register_error_false_password));
			this.passwordView.requestFocus();
			return false;
		}

		if (TextUtils.isEmpty(email)) {
			this.emailView.setError(getString(R.string.register_error_field_required));
			this.emailView.requestFocus();
			return false;
		} else if (!email.contains("@")) {
			this.emailView.setError(getString(R.string.register_error_invalid_email));
			this.emailView.requestFocus();
			return false;
		}

		if (TextUtils.isEmpty(name)) {
			this.emailView.setError(getString(R.string.register_error_field_required));
			this.nameView.requestFocus();
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
					RegisterActivity.this.loginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});

			this.loginFormView.setVisibility(View.VISIBLE);
			this.loginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					RegisterActivity.this.loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});
		} else {
			this.loginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			this.loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public void returnSuccess() {
		Intent result = new Intent();
		this.setResult(LoginActivity.RESULT_REGISTERED, result);
		this.finish();
	}

	public void setRegistrationFail() {
		this.emailView.setError(getString(R.string.register_error_invalid_email));
		this.emailView.requestFocus();
	}

	@Override
	public void handleMessage(Message message) {
		UiMessageType type = UiMessageType.values()[message.what];
		switch (type) {
			case UI_MESSAGE_REGISTERED: {
				switch ((RegisteredPacket.Status) message.obj) {
					case SUCCESS:
						this.returnSuccess();
						break;
					case FAILURE:
						this.setRegistrationFail();
						break;
				}
				return;
			}
			default:
				throw new IllegalArgumentException("Wrong message  type" + type);
		}
	}

	private class RegisterTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			if (!RegisterActivity.this.isConnected())
				return null;

			RegisterActivity.this.getClient().register(email, password, name, RegisterActivity.this);
			return null;
		}

		@Override
		protected void onCancelled() {
			RegisterActivity.this.registerTask = null;

			showProgress(false);
		}
	}
}
