package com.open.schedule.io.packet.client;

import com.open.schedule.io.packet.ClientPacket;

import java.io.IOException;

public class CreateTaskPacket extends ClientPacket {
	public CreateTaskPacket(Integer taskId, Integer tableId, Long time, String name, String description, String startDate, String endDate, String startTime, String endTime) throws IOException {
		super(ClientPacket.Type.CREATE_TASK);
		this.writeInt(taskId);
		this.writeInt(tableId);
		this.writeLong(time);
		this.writeString(name);
		this.writeString(description);
		this.writeFixedString(startDate);
		this.writeFixedString(endDate);
		this.writeFixedString(startTime);
		this.writeFixedString(endTime);
	}
}
