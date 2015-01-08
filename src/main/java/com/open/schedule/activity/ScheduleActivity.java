package com.open.schedule.activity;

import android.app.Activity;

import com.open.schedule.account.Account;
import com.open.schedule.app.ScheduleApplication;
import com.open.schedule.io.Client;
import com.open.schedule.io.ServerConnection;

public class ScheduleActivity extends Activity {
	public Account getAccount() {
		return ((ScheduleApplication) this.getApplication()).account;
	}

	public Client getClient() {
		return ((ScheduleApplication) this.getApplication()).client;
	}

	public ServerConnection getConnector() {
		return ((ScheduleApplication) this.getApplication()).connector;
	}

	protected boolean isConnected() {
		return ((ScheduleApplication) this.getApplication()).isConnected();
	}
}
