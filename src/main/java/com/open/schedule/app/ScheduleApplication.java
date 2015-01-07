package com.open.schedule.app;

import android.app.Application;
import android.util.Log;

import com.open.schedule.config.Config;
import com.open.schedule.io.Client;
import com.open.schedule.io.PacketDecoder;
import com.open.schedule.io.ServerConnector;
import com.open.schedule.storage.database.Database;

public class ScheduleApplication extends Application {
	public Client client;
	public Database database;
	public ServerConnector connector;

	@Override
	public void onCreate() {
		this.database = new Database(this);
		this.client = new Client(this.database);

		this.connector = new ServerConnector(new PacketDecoder(), this.client);
		this.connector.tryConnect(Config.HOST, Config.PORT);

		Log.d("ScheduleApplication", "Started");
	}

	public boolean isConnected() {
		return this.connector.isConnected();
	}

}
