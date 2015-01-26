package com.open.schedule.io.packet.client;

import com.open.schedule.io.packet.ClientPacket;

public class LoginPacket extends ClientPacket {
	public LoginPacket(String name, String password, long lastSyncTime) {
		super(ClientPacket.Type.LOGIN);
		this.write(name);
		this.write(password);
		this.write(lastSyncTime);
	}
}
