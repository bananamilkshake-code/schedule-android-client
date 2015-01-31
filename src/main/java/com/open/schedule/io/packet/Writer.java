package com.open.schedule.io.packet;

import java.nio.ByteBuffer;

public class Writer {
	private final ByteBuffer buffer;

	protected Writer(int size) {
		this.buffer =  ByteBuffer.allocate(size);
	}

	public short getSize() {
		return (short) this.buffer.position();
	}

	public ByteBuffer getBuffer() {
		return this.buffer;
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

	protected void write(Writable writable) {
		writable.write(this);
	}
}
