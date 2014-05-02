package io.packet.client;

import io.packet.ClientPacket;

import java.io.IOException;

public class CreateComment extends ClientPacket {
	public CreateComment(Integer tableId, Integer taskId, Long time, String comment) throws IOException {
		super(ClientPacket.Type.COMMENTARY);
		this.writeInt(tableId);
		this.writeInt(taskId);
		this.writeLong(time);
		this.writeString(comment);
	}
}
