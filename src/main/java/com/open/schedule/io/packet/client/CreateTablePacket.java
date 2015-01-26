package com.open.schedule.io.packet.client;

import com.open.schedule.io.packet.ClientPacket;

public class CreateTablePacket extends ClientPacket {
	public CreateTablePacket(Integer tableId, Long time, String name, String description) {
		super(ClientPacket.Type.CREATE_TABLE);
		this.write(tableId);
		this.write(time);
		this.write(name);
		this.write(description);
	}
}
