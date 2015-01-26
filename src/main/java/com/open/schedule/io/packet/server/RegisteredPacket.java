package com.open.schedule.io.packet.server;

import com.open.schedule.io.packet.ServerPacket;

public class RegisteredPacket extends ServerPacket {
	public enum Status {
		SUCCESS,
		FAILURE
	}

	public Status status;

	public RegisteredPacket(byte[] data) {
		super(ServerPacket.Type.REGISTERED, data);
	}

	@Override
	public void init() {
		this.status = Status.values()[this.getByte()];
	}
}
