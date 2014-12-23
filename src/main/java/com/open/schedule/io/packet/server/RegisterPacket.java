package com.open.schedule.io.packet.server;

import com.open.schedule.io.packet.ServerPacket;

public class RegisterPacket extends ServerPacket {
	public enum Status {
		SUCCESS,
		FAILURE
	}

	public Status status;
	
	public RegisterPacket() {
		super(ServerPacket.Type.REGISTER);
	}

	@Override
	public void init(char[] data) {
		this.status = Status.values()[data[0]];	
	}
}
