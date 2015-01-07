package com.open.schedule.activity;

import android.app.Activity;

import com.open.schedule.app.ScheduleApplication;
import com.open.schedule.io.Client;
import com.open.schedule.io.ServerConnector;

public class ScheduleActivity extends Activity {
	public Client getClient() {
		return ((ScheduleApplication) this.getApplication()).client;
	}

	public ServerConnector getConnector() {
		return ((ScheduleApplication) this.getApplication()).connector;
	}

	protected boolean isConnected() {
		return ((ScheduleApplication) this.getApplication()).isConnected();
	}
}
