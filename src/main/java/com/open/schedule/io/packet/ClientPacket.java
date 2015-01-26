package com.open.schedule.io.packet;

import java.nio.ByteBuffer;

public abstract class ClientPacket implements Packet {
	public static final int MAX_PACKET_SIZE = Short.MAX_VALUE;

	public final Type type;

	private final ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);

	public ClientPacket(Type type) {
		this.type = type;
	}

	public short getSize() {
		return (short) this.buffer.position();
	}

	public byte[] getBuffer() {
		return this.buffer.array();
	}

	protected void write(byte type, Object data) {
		if (data == null)
			return;

		this.write(type);

		if (data instanceof Byte) {
			this.write((Byte) data);
		} else if (data instanceof Short) {
			this.write((Short) data);
		} else if (data instanceof Integer) {
			this.write((Integer) data);
		} else if (data instanceof Long) {
			this.write((Long) data);
		} else if (data instanceof String) {
			this.write((String) data);
		}
	}

	protected void write(Byte value) {
		this.buffer.put(value);
	}

	protected void write(Short value) {
		this.buffer.putShort(value);
	}

	protected void write(Integer value) {
		this.buffer.putInt(value);
	}

	protected void write(Long value) {
		this.buffer.putLong(value);
	}

	protected void write(String value) {
		this.buffer.putShort((short) (Byte.SIZE * value.length()));
		this.buffer.put(value.getBytes());
	}

	public enum Type {
		REGISTER,
		LOGIN,
		CREATE_TABLE,
		CREATE_TASK,
		NOT_USED_TABLE_CHANGE,
		NOT_USED_TASK_CHANGE,
		NOT_USED_PERMISSION,
		NOT_USED_COMMENTARY
	}
}
