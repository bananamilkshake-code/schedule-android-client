package io;

import java.util.logging.Logger;

import io.packet.ServerPacket;
import io.packet.server.RegisterPacket;
import io.packet.server.LoginPacket;
import config.Config;

public class Client extends TCPClient {
	private boolean logged = false;
	
	public Client() throws Exception {
		super(Config.host, Config.port);		
	}

	public void recv(ServerPacket clientPacket) {
		switch (clientPacket.getType()) {
		case REGISTER:
		case LOGIN:
			if (isLogged()) {
				System.out.println("Recieved auth packets when already logged in");
				System.exit(1);
			}
			break;
		default:
			if (!isLogged()) {
				System.out.println("Recieved packets when not logged in");
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
				System.out.println("Successfully registered in");
				return;				
			case FAILURE:
				System.out.println("Username has already being used");
				return;
			}
			break;
		case LOGIN:
			LoginPacket login = (LoginPacket)clientPacket;
			switch (login.status) {
			case SUCCESS:
				System.out.println("Successfully logged in");
				logged = true;
				return;
			case FAILURE:
				System.out.println("Wrong username or password");
				return;
			}
			break;
		default:
			break;
		}
	}
	
	public boolean isLogged() {
		return logged;
	}
}
