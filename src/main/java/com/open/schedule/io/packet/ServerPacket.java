package com.open.schedule.io.packet;

import com.open.schedule.io.packet.server.*;

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
		case GLOBAL_TABLE_ID:
			return new GlobalTableIdPacket();
		case GLOBAL_TASK_ID:
			return new GlobalTaskIdPacket();
		case CHANGE_TABLE:
			return new ChangeTablePacket();
		case CHANGE_TASK:
			return new ChangeTaskPacket();
		case COMMENTARY:
			return new CommentaryPacket();
		case PERMISSION:
			return new PermissionPacket();
		case USER:
			return new UserPacket();
		}
		
		return null;
	}

	protected ServerPacket(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public abstract void init(char[] data);
}
