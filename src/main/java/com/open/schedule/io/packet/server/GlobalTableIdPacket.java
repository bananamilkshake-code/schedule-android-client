package com.open.schedule.io.packet.server;

import com.open.schedule.io.packet.ServerPacket;
import com.open.schedule.utility.Utility;

public class GlobalTableIdPacket extends ServerPacket {
	public Integer tableId;
	public Integer tableGlobalId;

	public GlobalTableIdPacket() {
		super(ServerPacket.Type.GLOBAL_TABLE_ID);
	}

	@Override
	public void init(byte[] data) {
		int offset = 0;
		this.tableId = Utility.getInt(data, offset);
		this.tableGlobalId = Utility.getInt(data, offset += Integer.SIZE / 8);
	}
}
