package com.open.schedule.app;

import android.app.Application;
import android.util.Log;

import com.open.schedule.account.Account;
import com.open.schedule.config.Config;
import com.open.schedule.io.Client;
import com.open.schedule.io.PacketDecoder;
import com.open.schedule.io.ServerConnection;

public class ScheduleApplication extends Application {
	public Account account;
	public Client client;
	public ServerConnection connector;

	@Override
	public void onCreate() {
		this.account = new Account();
		this.client = new Client(this.account);
		this.account.setClient(this.client);

		this.connector = new ServerConnection(new PacketDecoder(), this.client, Config.HOST, Config.PORT);
		this.connector.tryConnect();

		Log.d("ScheduleApplication", "Started");
	}

	public boolean isConnected() {
		return this.connector.isConnected();
	}
}
