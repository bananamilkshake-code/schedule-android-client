package io.packet;

import io.packet.server.*;

public abstract class ServerPacket extends Packet {
	public enum Type {
		REGISTER,
		LOGIN,
		GLOBAL_TABLE_ID,		// Локальная база данных клиента занесена в глобальную базу даных
		GLOBAL_TASK_ID,			// Локальное задание клиента занесено в глобальную базу данных
		CHANGE_TABLE,			// Так же используется для создания новой таблицы
		CHANGE_TASK,			// Так же используется для создания нового расписания
		PERMISSION,
		COMMENTARY,
		USER
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
