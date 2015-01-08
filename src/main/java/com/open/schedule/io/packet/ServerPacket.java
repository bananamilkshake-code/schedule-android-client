package com.open.schedule.io.packet;

import com.open.schedule.io.packet.server.LoggedPacket;
import com.open.schedule.io.packet.server.TablePacket;
import com.open.schedule.io.packet.server.TaskPacket;
import com.open.schedule.io.packet.server.RegisteredPacket;

public abstract class ServerPacket extends Packet {
	public enum Type {
		REGISTERED,
		LOGGED,
		NOT_USED_GLOBAL_TABLE_ID,        // Локальная база данных клиента занесена в глобальную базу даных
		NOT_USED_GLOBAL_TASK_ID,            // Локальное задание клиента занесено в глобальную базу данных
		TABLE,
		TASK,
		NOT_USED_PERMISSION,
		NOT_USED_COMMENTARY,
		NOT_USED_USER
	}

	private final Type type;

	protected ServerPacket(Type type) {
		this.type = type;
	}

	public static ServerPacket get(Type type) {
		switch (type) {
			case REGISTERED:
				return new RegisteredPacket();
			case LOGGED:
				return new LoggedPacket();
			case TABLE:
				return new TablePacket();
			case TASK:
				return new TaskPacket();
		}

		return null;
	}

	public Type getType() {
		return type;
	}

	public abstract void init(byte[] data);
}
