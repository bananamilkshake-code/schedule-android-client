package io.packet.client;

import java.io.IOException;
import io.packet.ClientPacket;

public class CreateTablePacket extends ClientPacket {
	public CreateTablePacket(Long time, String name, String description) throws IOException {
		super(ClientPacket.Type.CREATE_TABLE);
		this.writeLong(time);
		this.writeString(name);
		this.writeString(description);
	}
}
