package com.open.schedule.io.packet;

public abstract class ClientPacket extends Writer implements Packet {
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

	public class Group extends Writer implements Writable {
		private int number = 0;

		public Group() {
			super(ClientPacket.MAX_PACKET_SIZE);
		}

		public void next() {
			this.number++;
		}

		@Override
		public void write(Writer writer) {
			writer.write(this.number);
			writer.getBuffer().put(this.getBuffer());
		}
	}

	public static final int MAX_PACKET_SIZE = Short.MAX_VALUE;

	public final Type type;

	public ClientPacket(Type type) {
		super(MAX_PACKET_SIZE);
		this.type = type;
	}
}
