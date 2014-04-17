package io;

import java.io.IOException;
import java.util.ArrayList;

import table.Table;
import android.util.Log;
import config.Config;
import events.objects.Event;
import io.packet.ServerPacket;
import io.packet.server.RegisterPacket;
import io.packet.server.LoginPacket;

public class Client extends TCPClient {	
	private static final Client INSTANCE = new Client();
	
	private boolean logged = false;
	private Integer id = 0;
	
	private ArrayList<Table> tables = new ArrayList<Table>();
	
	protected Client() {
		super(Config.host, Config.port);
	}
	
	public static Client getInstance() {
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
		this.authorise(username, password);
	}
	
	public void authorise(String username, String password) {
		try {
			INSTANCE.send(new io.packet.client.LoginPacket(username, password));
		} catch (IOException e) {
			Log.w("Client", "Authorization error", e);
		}
	}

	public void createNewTable(String tableName, String tableDesc) {
		try {
			tables.add(new Table(this.id, tableName, tableDesc));
			INSTANCE.send(new io.packet.client.CreateTablePacket(tableName, tableDesc));
		} catch (IOException e) {
			Log.w("Client", "New table creation error", e);
		}
	}
}
