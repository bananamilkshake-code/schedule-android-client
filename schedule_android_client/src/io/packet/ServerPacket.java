package io.packet;

import io.packet.server.*;

public abstract class ServerPacket extends Packet {
	public enum Type {
		REGISTER,
		LOGIN
	}

	private final Type type;

	public static ServerPacket get(Type type) {
		switch (type) {
		case REGISTER:
			return new RegisterPacket();
		case LOGIN:
			return new LoginPacket();
		default:
			return null;
		}
	}

	protected ServerPacket(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public abstract void init(char[] data);
}
