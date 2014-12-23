package com.open.schedule.io.packet.server;

import com.open.schedule.utility.Utility;
import com.open.schedule.io.packet.ServerPacket;

public class LoginPacket extends ServerPacket {
	public enum Status {
		SUCCESS,
		FAILURE
	}

	public Status status;
	public Integer id;	
	
	public LoginPacket() {
		super(ServerPacket.Type.LOGIN);
	}

	@Override
	public void init(char[] data) {
		this.status = Status.values()[data[0]];
		
		if (this.status == Status.SUCCESS) {
			this.id = Utility.getInt(data, 1);
		}
	}
}
