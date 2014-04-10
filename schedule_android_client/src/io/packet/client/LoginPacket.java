package io.packet.client;

import java.io.DataOutputStream;
import java.io.IOException;

import io.packet.ClientPacket;

public class LoginPacket extends ClientPacket {
	private final String name;
	private final String password;

	public LoginPacket(String name, String password) {
		super(ClientPacket.Type.LOGIN);

		this.name = name;
		this.password = password;
	}

	@Override
	public void write(DataOutputStream stream) throws IOException {
		writeString(stream, name);
		writeString(stream, password);
	}

	@Override
	public short getSize() {
		return (short)(Short.SIZE + name.length() * Byte.SIZE + Short.SIZE + password.length() * Byte.SIZE);
	}
}
