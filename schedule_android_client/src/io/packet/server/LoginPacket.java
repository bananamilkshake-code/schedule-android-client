package io.packet.server;

import io.packet.ServerPacket;

public class LoginPacket extends ServerPacket {
	public enum Status {
		SUCCESS,
		FAILURE
	}

	public Status status;
	
	public LoginPacket() {
		super(ServerPacket.Type.LOGIN);
	}

	@Override
	public void init(char[] data) {
		this.status = Status.values()[data[0]];
	}
}
