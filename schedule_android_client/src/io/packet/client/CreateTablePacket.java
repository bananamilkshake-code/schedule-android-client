package io.packet.client;

import java.io.IOException;
import io.packet.ClientPacket;

public class CreateTablePacket extends ClientPacket {
	public CreateTablePacket(String name, String description) throws IOException {
		super(ClientPacket.Type.CREATE_TABLE);
		writeString(name);
		writeString(description);
	}
}
