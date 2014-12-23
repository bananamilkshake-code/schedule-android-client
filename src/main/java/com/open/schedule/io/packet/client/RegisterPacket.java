package com.open.schedule.io.packet.client;

import java.io.IOException;

import com.open.schedule.io.packet.ClientPacket;

public class RegisterPacket extends ClientPacket  {
	public RegisterPacket(String name, String password) throws IOException {
		super(ClientPacket.Type.REGISTER);
		writeString(name);
		writeString(password);
	}
}
