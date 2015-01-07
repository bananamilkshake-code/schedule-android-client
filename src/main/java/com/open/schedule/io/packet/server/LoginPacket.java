package com.open.schedule.io.packet.server;

import com.open.schedule.io.packet.ServerPacket;
import com.open.schedule.utility.Utility;

public class LoginPacket extends ServerPacket {
	public Status status;
	public Integer id;
	public LoginPacket() {
		super(ServerPacket.Type.LOGIN);
	}

	@Override
	public void init(byte[] data) {
		this.status = Status.values()[data[0]];

		if (this.status == Status.SUCCESS) {
			this.id = Utility.getInt(data, 1);
		}
	}

	public enum Status {
		SUCCESS,
		FAILURE
	}
}
