package com.open.schedule.io.packet.server;

import com.open.schedule.utility.Utility;
import com.open.schedule.io.packet.ServerPacket;

public class GlobalTaskIdPacket extends ServerPacket {
	public Integer taskId;
	public Integer taskGlobalId;
	public Integer tableGlobalId;
	
	public GlobalTaskIdPacket() {
		super(ServerPacket.Type.GLOBAL_TASK_ID);
	}

	@Override
	public void init(char[] data) {
		int offset = 0;
		this.taskId = Utility.getInt(data, offset);
		this.taskGlobalId = Utility.getInt(data, offset += Integer.SIZE / 8);
		this.tableGlobalId = Utility.getInt(data, offset += Integer.SIZE / 8);
	}
}
