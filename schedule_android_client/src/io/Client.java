package io;

import android.util.Log;
import io.packet.ServerPacket;
import io.packet.server.RegisterPacket;
import io.packet.server.LoginPacket;
import config.Config;

public class Client extends TCPClient {
	private static Client INSTANCE = null;
	
	private boolean logged = false;
	
	public static Client getInstance() {
		if (INSTANCE == null)
			INSTANCE = new Client();
		
		return INSTANCE;
	}
	
	private Client() {
		super(Config.host, Config.port);
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
			case FAILURE:
				Log.w("Recv", "Wrong username or password");
			}
			break;
		default:
			break;
		}
	}
	
	public static boolean isLogged() {
		return INSTANCE.logged;
	}
}
