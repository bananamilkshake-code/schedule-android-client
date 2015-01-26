package com.open.schedule.io.packet.server;

import com.open.schedule.io.packet.ServerPacket;
import com.open.schedule.utility.Utility;

public class LoggedPacket extends ServerPacket {
	public enum Status {
		SUCCESS,
		FAILURE
	}

	public Status status;
	public Integer id;

	public LoggedPacket(byte[] data) {
		super(ServerPacket.Type.LOGGED, data);
	}

	@Override
	public void init() {
		this.status = Status.values()[this.getByte()];

		if (this.status == Status.SUCCESS) {
			this.id = this.getInt();
		}
	}
}
