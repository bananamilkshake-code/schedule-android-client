package io.packet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class ClientPacket extends Packet {
	public static final int MAX_PACKET_SIZE = Short.MAX_VALUE;
	private final String UTF8_CHARSET = "UTF-8";

	private ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);

	public enum Type {
		REGISTER,
		LOGIN,
		CREATE_TABLE
	}

	private final Type type;

	public ClientPacket(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void write(DataOutputStream outToServer) throws IOException {
		short size = this.getSize();
		int dataOffset = PACKET_TYPE_BYTE + PACKET_SIZE_BYTE;
		byte data[] = new byte[dataOffset + size];

		data[0] = (byte)(this.getType().ordinal());
		data[1] = (byte)((size * Byte.SIZE >> 8) & 0xff);
		data[2] = (byte)(size * Byte.SIZE & 0xff);
		System.arraycopy(buffer.array(), 0, data, dataOffset, size);

		outToServer.write(data);
	}
	public short getSize() {
		return (short) this.buffer.position();
	}
	
	protected void writeString(String string) throws IOException {
		this.buffer.putShort((short)(2 * Byte.SIZE * string.length()));
		this.buffer.put(string.getBytes(UTF8_CHARSET));
	}
}
