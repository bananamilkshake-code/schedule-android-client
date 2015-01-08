package com.open.schedule.io.packet.server;

import com.open.schedule.io.packet.ServerPacket;

public class RegisteredPacket extends ServerPacket {
	public Status status;

	public RegisteredPacket() {
		super(ServerPacket.Type.REGISTERED);
	}

	@Override
	public void init(byte[] data) {
		this.status = Status.values()[data[0]];
	}

	public enum Status {
		SUCCESS,
		FAILURE
	}
}
