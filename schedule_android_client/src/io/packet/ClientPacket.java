package io.packet;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class ClientPacket extends Packet {

	public enum Type {
		REGISTER,
		LOGIN,
		CREATE_TABLE
	}

	private final Type type;

	public ClientPacket(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public abstract void write(DataOutputStream outToServer) throws IOException;
	public abstract short getSize();
	
	protected void writeString(DataOutputStream outToServer, String string) throws IOException {
		outToServer.writeShort(Byte.SIZE * string.length());
		outToServer.writeBytes(string);
	}
}
