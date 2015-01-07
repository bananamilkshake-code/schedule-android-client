package com.open.schedule.app;

import android.app.Application;
import android.util.Log;

import com.open.schedule.config.Config;
import com.open.schedule.io.Client;
import com.open.schedule.io.PacketDecoder;
import com.open.schedule.io.ServerConnection;
import com.open.schedule.storage.database.Database;

public class ScheduleApplication extends Application {
	public Client client;
	public Database database;
	public ServerConnection connector;

	@Override
	public void onCreate() {
		this.database = new Database(this);
		this.client = new Client(this.database);

		this.connector = new ServerConnection(new PacketDecoder(), this.client, Config.HOST, Config.PORT);
		this.connector.tryConnect();

		Log.d("ScheduleApplication", "Started");
	}

	public boolean isConnected() {
		return this.connector.isConnected();
	}
}
