package com.open.schedule.io.packet.client;

import com.open.schedule.io.packet.ClientPacket;

import java.util.Date;

public class CreateTaskPacket extends ClientPacket {
	private enum Types {
		NAME,
		DESCRIPTION,
		START_DATE,
		END_DATE,
		START_TIME,
		END_TIME,
		PERIOD
	}

	public CreateTaskPacket(int taskId, int tableId, long time, String name, String description,
							Date startDate, Date endDate, Date startTime, Date endTime, Short period) {
		super(ClientPacket.Type.CREATE_TASK);
		this.write(taskId);
		this.write(tableId);
		this.write(time);

		this.write((byte) Types.NAME.ordinal(), name);
		this.write((byte) Types.DESCRIPTION.ordinal(), description);
		this.write((byte) Types.START_DATE.ordinal(), startDate.toString());
		this.write((byte) Types.END_DATE.ordinal(), endDate.toString());
		this.write((byte) Types.START_TIME.ordinal(), startTime.toString());
		this.write((byte) Types.END_TIME.ordinal(), endTime.toString());
		this.write((byte) Types.PERIOD.ordinal(), period.toString());
	}
}
