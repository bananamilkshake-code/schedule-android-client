package io.packet.client;

import java.io.DataOutputStream;
import java.io.IOException;

import io.packet.ClientPacket;

public class CreateTablePacket extends ClientPacket {
	private final String name;
	private final String description;
	
	public CreateTablePacket(String name, String description) {
		super(ClientPacket.Type.CREATE_TABLE);

		this.name = name;
		this.description = description;
	}

	@Override
	public void write(DataOutputStream outToServer) throws IOException {
		writeString(outToServer, name);
		writeString(outToServer, description);
	}

	@Override
	public short getSize() {
		return (short)(Short.SIZE + name.length() * Byte.SIZE + Short.SIZE + description.length() * Byte.SIZE);
	}
}
