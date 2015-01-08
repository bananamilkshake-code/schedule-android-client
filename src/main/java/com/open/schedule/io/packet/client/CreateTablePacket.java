package com.open.schedule.io.packet.client;

import com.open.schedule.io.packet.ClientPacket;

import java.io.IOException;

public class CreateTablePacket extends ClientPacket {
	public CreateTablePacket(Integer tableId, Long time, String name, String description) {
		super(ClientPacket.Type.CREATE_TABLE);
		this.writeInt(tableId);
		this.writeLong(time);
		this.writeString(name);
		this.writeString(description);
	}
}
