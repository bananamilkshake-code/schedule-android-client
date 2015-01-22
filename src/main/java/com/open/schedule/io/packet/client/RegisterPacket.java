package com.open.schedule.io.packet.client;

import com.open.schedule.io.packet.ClientPacket;

public class RegisterPacket extends ClientPacket {
	public RegisterPacket(String name, String password) {
		super(ClientPacket.Type.REGISTER);
		writeString(name);
		writeString(password);
	}
}
