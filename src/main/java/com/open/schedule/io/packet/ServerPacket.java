package com.open.schedule.io.packet;

import com.open.schedule.io.packet.server.ChangeTablePacket;
import com.open.schedule.io.packet.server.ChangeTaskPacket;
import com.open.schedule.io.packet.server.CommentaryPacket;
import com.open.schedule.io.packet.server.GlobalTableIdPacket;
import com.open.schedule.io.packet.server.GlobalTaskIdPacket;
import com.open.schedule.io.packet.server.LoginPacket;
import com.open.schedule.io.packet.server.PermissionPacket;
import com.open.schedule.io.packet.server.RegisterPacket;
import com.open.schedule.io.packet.server.UserPacket;

public abstract class ServerPacket extends Packet {
	private final Type type;

	protected ServerPacket(Type type) {
		this.type = type;
	}

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

	public Type getType() {
		return type;
	}

	public abstract void init(byte[] data);

	public enum Type {
		REGISTER,
		LOGIN,
		GLOBAL_TABLE_ID,        // Локальная база данных клиента занесена в глобальную базу даных
		GLOBAL_TASK_ID,            // Локальное задание клиента занесено в глобальную базу данных
		CHANGE_TABLE,            // Так же используется для создания новой таблицы
		CHANGE_TASK,            // Так же используется для создания нового расписания
		PERMISSION,
		COMMENTARY,
		USER
	}
}
