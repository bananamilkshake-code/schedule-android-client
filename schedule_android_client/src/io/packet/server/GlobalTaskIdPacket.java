package io.packet.server;

import utility.Utility;
import io.packet.ServerPacket;

public class GlobalTaskIdPacket extends ServerPacket {
	public Integer taskId;
	public Integer taskGlobalId;
	public Integer tableGlobalId;
	
	GlobalTaskIdPacket() {
		super(ServerPacket.Type.GLOBAL_TASK_ID);
	}

	@Override
	public void init(char[] data) {
		int offset = 0;
		this.taskId = Utility.getInt(data, offset);
		this.taskGlobalId = Utility.getInt(data, offset += Integer.SIZE);
		this.tableGlobalId = Utility.getInt(data, offset += Integer.SIZE);
	}
}
