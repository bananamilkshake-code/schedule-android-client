package io;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import storage.database.Database;
import storage.tables.Table;
import utility.Utility;
import android.util.Log;
import config.Config;
import events.objects.Event;
import io.packet.ServerPacket;
import io.packet.server.RegisterPacket;
import io.packet.server.LoginPacket;

public class Client extends TCPClient {	
	private static Client INSTANCE = null;
	
	private boolean logged = false;
	private Integer id = 0;
	
	private HashMap<Integer, Table> tables = new HashMap<Integer, Table>();
	
	private Database database = null;
	
	protected Client(Database database) {
		super(Config.host, Config.port);
		this.database = database;

		database.loadTables(tables);
	}

	public static void createInstance(Database database) {
		INSTANCE = new Client(database);
	}
	
	public static Client getInstance() {
		assert INSTANCE != null;
		return INSTANCE;
	}

	public void recv(ServerPacket clientPacket) {
		switch (clientPacket.getType()) {
		case REGISTER:
		case LOGIN:
			if (isLogged()) {
				Log.e("Recv", "Recieved auth packets when already logged in");
				System.exit(1);
			}
			break;
		default:
			if (!isLogged()) {
				Log.e("Recv", "Recieved packets when not logged in");
				System.exit(1);	
			}
			break;
		}

		switch (clientPacket.getType())
		{
		case REGISTER:
			RegisterPacket register = (RegisterPacket)clientPacket;	
			switch (register.status) {
			case SUCCESS:
				Log.d("Recv", "Successfully registered in");
				return;	
			case FAILURE:
				Log.w("Recv", "Username has already being used");
				return;
			}
			break;
		case LOGIN:
			LoginPacket login = (LoginPacket)clientPacket;
			switch (login.status) {
			case SUCCESS:
				Log.d("Recv", "Successfully logged in");
				logged = true;
				break;
			case FAILURE:
				Log.w("Recv", "Wrong username or password");
				break;
			}
			new Event(this, Event.Type.LOGIN, (Object)login.status);
			break;
		default:
			break;
		}
	}
	
	public boolean isLogged() {
		return logged;
	}

	public void loadAuthParams() {
		String username = "l@m.c";
		String password = "qqqq";
		this.login(username, password);
	}
	
	public void login(String username, String password) {
		try {
			send(new io.packet.client.LoginPacket(username, password));
		} catch (IOException e) {
			Log.w("Client", "Authorization error", e);
		}
	}

	public void createNewTable(String name, String description) {
		try {
			Table table = new Table(this.id, Utility.getUnixTime(), name, description);
			Integer newTableId = database.createTable(table);
			tables.put(newTableId, table);
			send(new io.packet.client.CreateTablePacket(name, description));
			Log.d("Client", "New table created " + newTableId);
		} catch (IOException e) {
			Log.w("Client", "New table creation error", e);
		}
	}
}
