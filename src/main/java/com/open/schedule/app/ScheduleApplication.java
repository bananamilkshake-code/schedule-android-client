package com.open.schedule.app;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.open.schedule.R;
import com.open.schedule.account.Account;
import com.open.schedule.config.Config;
import com.open.schedule.io.Client;
import com.open.schedule.io.PacketDecoder;
import com.open.schedule.io.ServerConnection;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ScheduleApplication extends Application {
	public Account account;
	public Client client;
	public ServerConnection connector;

	private final static ConnectionRetryExecutor scheduler;

	static {
		scheduler = new ConnectionRetryExecutor();
	}

	@Override
	public void onCreate() {
		this.account = new Account();
		this.client = new Client(this.account);
		this.account.setClient(this.client);

		this.connector = new ServerConnection(this, new PacketDecoder(), this.client, Config.HOST, Config.PORT);

		new ConnectionAsyncTask().execute();

		Log.d("ScheduleApplication", "Started");
	}

	public boolean isConnected() {
		return this.connector.isConnected();
	}

	public class ConnectionAsyncTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			ScheduleApplication.this.connector.tryConnect();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (ScheduleApplication.this.isConnected()) {
				Toast.makeText(ScheduleApplication.this.getBaseContext(), R.string.toast_connection_up, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(ScheduleApplication.this.getBaseContext(), "Не смог", Toast.LENGTH_SHORT).show();

				new ConnectionAsyncTask().executeOnExecutor(ScheduleApplication.scheduler);
			}
		}
	}

	private static class ConnectionRetryExecutor extends ScheduledThreadPoolExecutor {
		private final static int CORE_POOL_SIZE = 1;
		private final static int RETRY_TIMEOUT = 30;

		public ConnectionRetryExecutor() {
			super(CORE_POOL_SIZE);
		}

		@Override
		public void execute (Runnable command) {
			schedule(command, RETRY_TIMEOUT, SECONDS);
		}
	}
}
