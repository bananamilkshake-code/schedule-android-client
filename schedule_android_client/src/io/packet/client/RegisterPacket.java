package io.packet.client;

import java.io.DataOutputStream;
import java.io.IOException;

import io.packet.ClientPacket;

public class RegisterPacket extends ClientPacket  {
	private final String name;
	private final String password;
	
	public RegisterPacket(String name, String password) {
		super(ClientPacket.Type.REGISTER);

		this.name = name;
		this.password = password;
	}

	public void write(DataOutputStream stream) throws IOException {
		writeString(stream, name);
		writeString(stream, password);
	}

	public short getSize() {
		return (short)(Short.SIZE + name.length() * Byte.SIZE + Short.SIZE + password.length() * Byte.SIZE);		
	}
}
