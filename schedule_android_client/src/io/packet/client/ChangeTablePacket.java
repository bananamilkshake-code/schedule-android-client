package io.packet.client;

import io.packet.ClientPacket;

import java.io.IOException;

public class ChangeTablePacket extends ClientPacket {
	public ChangeTablePacket(Integer tableId, Long time, String name, String description) throws IOException {
		super(ClientPacket.Type.TABLE_CHANGE);
		this.writeInt(tableId);
		this.writeLong(time);
		this.writeString(name);
		this.writeString(description);
	}
}
