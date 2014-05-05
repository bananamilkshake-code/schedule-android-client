package io.packet.client;

import java.io.IOException;
import io.packet.ClientPacket;

public class CreateTablePacket extends ClientPacket {
	public CreateTablePacket(Integer tableId, Long time, String name, String description) throws IOException {
		super(ClientPacket.Type.CREATE_TABLE);
		this.writeInt(tableId);
		this.writeLong(time);
		this.writeString(name);
		this.writeString(description);
	}
}
