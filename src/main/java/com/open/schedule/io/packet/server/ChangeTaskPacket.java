package com.open.schedule.io.packet.server;

import com.open.schedule.utility.Utility;
import com.open.schedule.io.packet.ServerPacket;

public class ChangeTaskPacket extends ServerPacket {
	public Integer tableGlobalId;
	public Integer taskGlobalId;
	public Long time;
	public Integer userId;
	public String name;
	public String description;
	public String startDate;
	public String endDate;
	public String startTime;
	public String endTime;
	public Integer period;

	public ChangeTaskPacket() {
		super(ServerPacket.Type.CHANGE_TASK);
	}

	@Override
	public void init(char[] data) {
		int offset = 0;
		this.tableGlobalId = Utility.getInt(data, offset);
		this.taskGlobalId = Utility.getInt(data, offset += Integer.SIZE / 8);
		this.time = Utility.getLong(data, offset += Integer.SIZE / 8);
		this.userId = Utility.getInt(data, offset += Long.SIZE / 8);
		Short nameLength = Utility.getShort(data, offset += Integer.SIZE / 8);
		this.name = new String(data, offset += Short.SIZE / 8, nameLength);
		Short descriptionLength = Utility.getShort(data, offset += nameLength);
		this.description = new String(data, offset += Short.SIZE / 8, descriptionLength);
		this.startDate = new String(data, offset += descriptionLength, 8);
		this.endDate = new String(data, offset += 8, 8);
		this.startTime = new String(data, offset += 8, 4);
		this.endDate = new String(data, offset += 4, 4);
		this.period = Integer.parseInt(new String(data, offset += 4, Integer.SIZE));
	}
}
