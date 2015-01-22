package com.open.schedule.io.packet.client;

import com.open.schedule.io.packet.ClientPacket;

public class CreateTaskPacket extends ClientPacket {
	public CreateTaskPacket(int taskId, int tableId, long time, String name, String description, String startDate, String endDate, String startTime, String endTime) {
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
