package com.open.schedule.io.packet.client;

import com.open.schedule.account.tables.TimeFormat;
import com.open.schedule.io.packet.ClientPacket;
import com.open.schedule.utility.Utility;

import java.util.Date;

public class CreateTaskPacket extends ClientPacket {
	private enum Fields {
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

		this.write((byte) Fields.NAME.ordinal(), name);
		this.write((byte) Fields.DESCRIPTION.ordinal(), description);
		this.write((byte) Fields.START_DATE.ordinal(), Utility.parseToString(startDate, TimeFormat.DATE_FORMATTER));
		this.write((byte) Fields.END_DATE.ordinal(), Utility.parseToString(endDate, TimeFormat.DATE_FORMATTER));
		this.write((byte) Fields.START_TIME.ordinal(), Utility.parseToString(startTime, TimeFormat.DATE_FORMATTER));
		this.write((byte) Fields.END_TIME.ordinal(), Utility.parseToString(endTime, TimeFormat.DATE_FORMATTER));
		this.write((byte) Fields.PERIOD.ordinal(), period);
	}
}
