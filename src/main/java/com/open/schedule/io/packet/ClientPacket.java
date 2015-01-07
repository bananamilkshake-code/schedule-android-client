package com.open.schedule.io.packet;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class ClientPacket extends Packet {
	public static final int MAX_PACKET_SIZE = Short.MAX_VALUE;
	private ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);

	public enum Type {
		REGISTER,
		LOGIN,
		CREATE_TABLE,
		CREATE_TASK,
		TABLE_CHANGE,
		TASK_CHANGE,
		PERMISSION,
		COMMENTARY
	}

	private final Type type;

	public ClientPacket(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public short getSize() {
		return (short) this.buffer.position();
	}

	public byte[] getBuffer() {
		return this.buffer.array();
	}
	
	protected void writeString(String value) throws IOException {
		this.buffer.putShort((short)(Byte.SIZE * value.length()));
		this.buffer.put(value.getBytes());
	}
	
	protected void writeFixedString(String value) throws IOException {
		this.buffer.put(value.getBytes());
	}
	
	protected void writeByte(Byte value) throws IOException {
		this.buffer.put(value);
	}
	
	protected void writeShort(Short value) throws IOException {
		this.buffer.putShort(value);
	}
	
	protected void writeInt(Integer value) throws IOException {
		this.buffer.putInt(value);
	}
	
	protected void writeLong(Long value) throws IOException {
		this.buffer.putLong(value);
	}
}
