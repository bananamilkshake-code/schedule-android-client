package com.open.schedule.io.packet.client;

import com.open.schedule.io.packet.ClientPacket;

import java.io.IOException;

public class LoginPacket extends ClientPacket {
	public LoginPacket(String name, String password) throws IOException {
		super(ClientPacket.Type.LOGIN);
		this.writeString(name);
		this.writeString(password);
	}
}
