package com.open.schedule.io.packet;

import com.open.schedule.io.packet.server.LoggedPacket;
import com.open.schedule.io.packet.server.TablePacket;
import com.open.schedule.io.packet.server.TaskPacket;
import com.open.schedule.io.packet.server.RegisteredPacket;

public abstract class ServerPacket implements Packet {
	public enum Type {
		REGISTERED(false),
		LOGGED(false),
		NOT_USED_GLOBAL_TABLE_ID,        // Локальная база данных клиента занесена в глобальную базу даных
		NOT_USED_GLOBAL_TASK_ID,         // Локальное задание клиента занесено в глобальную базу данных
		TABLE,
		TASK,
		NOT_USED_PERMISSION,
		NOT_USED_COMMENTARY,
		NOT_USED_USER;

		public final boolean needLogged;

		Type() {
			this(true);
		}

		Type(boolean needLogged) {
			this.needLogged = needLogged;
		}
	}

	private final Type type;
	private final byte[] buffer;
	private int offset;

	protected ServerPacket(Type type, byte[] data) {
		this.type = type;
		this.buffer = data;
		this.offset = 0;
	}

	public static ServerPacket get(Type type, byte[] data) {
		ServerPacket packet = null;

		switch (type) {
			case REGISTERED:
				packet = new RegisteredPacket(data);
				break;
			case LOGGED:
				packet = new LoggedPacket(data);
				break;
			case TABLE:
				packet = new TablePacket(data);
				break;
			case TASK:
				packet = new TaskPacket(data);
				break;
		}

		packet.init();
		return packet;
	}

	public Type getType() {
		return type;
	}

	public abstract void init();

	protected Byte getByte() {
		return new Byte(buffer[this.offset++]);
	}

	protected Short getShort() {
		Short result = new Short((short) 0);

		for (int i = 0; i < Short.SIZE / 8; i++)
			result = (short) ((result << 8) | buffer[this.offset++]);

		return result;
	}

	protected Integer getInt() {
		Integer result = 0;

		for (int i = 0; i < Integer.SIZE / 8; i++)
			result = (result << 8) | buffer[this.offset++];

		return result;
	}

	protected Long getLong() {
		Long result = Integer.valueOf(0).longValue();

		for (int i = 0; i < Long.SIZE / 8; i++)
			result = (result << 8) | this.buffer[this.offset++];

		return result;
	}

	protected String getString() {
		short length = this.getShort();

		String result = new String(this.buffer, this.offset, length);

		this.offset += length;

		return result;
	}
}
