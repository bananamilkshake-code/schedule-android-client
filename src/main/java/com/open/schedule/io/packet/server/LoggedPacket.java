package com.open.schedule.io.packet.server;

import com.open.schedule.io.packet.ServerPacket;
import com.open.schedule.utility.Utility;

public class LoggedPacket extends ServerPacket {
	public Status status;
	public Integer id;
	public LoggedPacket() {
		super(ServerPacket.Type.LOGGED);
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
