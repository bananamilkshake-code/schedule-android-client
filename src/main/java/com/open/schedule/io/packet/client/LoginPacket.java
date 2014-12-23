package com.open.schedule.io.packet.client;

import java.io.IOException;
import com.open.schedule.io.packet.ClientPacket;

public class LoginPacket extends ClientPacket {
	public LoginPacket(String name, String password) throws IOException {
		super(ClientPacket.Type.LOGIN);
		this.writeString(name);
		this.writeString(password);
	}
}
