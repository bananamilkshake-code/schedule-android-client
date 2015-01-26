package com.open.schedule.io.packet.server;

import com.open.schedule.io.packet.ServerPacket;

public class TaskPacket extends ServerPacket {
	public int tableId;
	public int taskId;
	public long time;
	public int creatorId;

	public String name;
	public String description;
	public String startDate;
	public String endDate;
	public String startTime;
	public String endTime;
	public Short period;

	public TaskPacket(byte[] data) {
		super(ServerPacket.Type.TASK, data);
	}

	@Override
	public void init() {
		this.tableId = this.getInt();
		this.taskId = this.getInt();
		this.time = this.getLong();
		this.creatorId = this.getInt();

		this.name = this.getString();
		this.description = this.getString();
		this.startDate = this.getString();
		this.endDate = this.getString();
		this.startTime = this.getString();
		this.endDate = this.getString();

		this.period = this.getShort();
	}
}
