package com.open.schedule.activity;

import com.open.schedule.R;
import com.open.schedule.io.packet.server.RegisterPacket;
import com.open.schedule.events.listeners.EventListener;
import com.open.schedule.events.objects.Event;
import com.open.schedule.events.objects.EventWarehouse;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends ScheduleActivity implements OnClickListener {
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

	private RegisterActivityLister registerListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);

		// Set up the login form.
		emailView = (EditText) findViewById(R.id.email);
		emailView.setText(email);

		passwordView = (EditText) findViewById(R.id.password);
		passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							register();
							return true;
						}
						return false;
					}
				});
		
		passwordVerifyView = (EditText) findViewById(R.id.password_verification);
		nameView = (EditText) findViewById(R.id.name);

		loginFormView = findViewById(R.id.login_form);
		loginStatusView = findViewById(R.id.login_status);
		loginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.register_button).setOnClickListener(this);
		
		registerListener = new RegisterActivityLister();
	}
	
	@Override
	protected void onStop() {
		registerListener.shutdown();
		super.onStop();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.register_button:
			register();
			break;
		}
	}

	private void register() {
		if (registerTask != null) {
			return;
		}

		// Reset errors.
		emailView.setError(null);
		passwordView.setError(null);
		passwordVerifyView.setError(null);
		nameView.setError(null);

		// Store values at the time of the login attempt.
		email = emailView.getText().toString();
		password = passwordView.getText().toString();
		passwordVerification = passwordVerifyView.getText().toString();
		name = nameView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(password)) {
			passwordView.setError(getString(R.string.register_error_field_required));
			focusView = passwordView;
			cancel = true;
		} else if (password.length() < 4) {
			passwordView.setError(getString(R.string.register_error_invalid_password));
			focusView = passwordView;
			cancel = true;
		} else if (TextUtils.isEmpty(passwordVerification)) {
			passwordVerifyView.setError(getString(R.string.register_error_no_verification));
			focusView = passwordVerifyView;
			cancel = true;
		} else if (!TextUtils.equals(password, passwordVerification)) {
			passwordVerifyView.getEditableText().clear();
			passwordView.setError(getString(R.string.register_error_false_password));
			focusView = passwordView;
			cancel = true;
		}

		if (TextUtils.isEmpty(email)) {
			emailView.setError(getString(R.string.register_error_field_required));
			focusView = emailView;
			cancel = true;
		} else if (!email.contains("@")) {
			emailView.setError(getString(R.string.register_error_invalid_email));
			focusView = emailView;
			cancel = true;
		}

		if (TextUtils.isEmpty(name)) {
			emailView.setError(getString(R.string.register_error_field_required));
			focusView = nameView;
			cancel = true;
		}

		if (cancel) {
			focusView.requestFocus();
		} else {
			loginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			registerTask = new RegisterTask();
			registerTask.execute();
		}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			loginStatusView.setVisibility(View.VISIBLE);
			loginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							loginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

			loginFormView.setVisibility(View.VISIBLE);
			loginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});
		} else {
			loginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	private void setRegistrationFail() {
		emailView.setError(getString(R.string.register_error_invalid_email));
		emailView.requestFocus();
	}
	
	private void returnSuccess() {
		Intent result = new Intent();
		setResult(LoginActivity.RESULT_REGISTERED, result);
		finish();
	}
	
	private class RegisterActivityLister implements EventListener {
		public RegisterActivityLister() {
			super();
			EventWarehouse.getInstance().addListener((EventListener) this, Event.Type.REGISTER);
		}

		@Override
		public void handle(Event event) {
			showProgress(false);
			switch(event.getType()) {
			case REGISTER:
				RegisterPacket.Status status = (RegisterPacket.Status) event.getData();
				switch (status) {
				case SUCCESS:
					returnSuccess();
					break;
				case FAILURE:
					setRegistrationFail();
					break;
				}
				break;
			default:
				break;
			}
		}

		public final void shutdown() {
			EventWarehouse.getInstance().removeListener((EventListener) this, Event.Type.REGISTER);
		}
	}

	private class RegisterTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			if (!RegisterActivity.this.isConnected())
				return null;

			RegisterActivity.this.getClient().register(email, password, name);
			return null;
		}

		@Override
		protected void onCancelled() {
			registerTask = null;
			showProgress(false);
		}
	}
}
