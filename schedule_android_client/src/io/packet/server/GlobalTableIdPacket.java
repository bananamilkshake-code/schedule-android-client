package io.packet.server;

import utility.Utility;
import io.packet.ServerPacket;

public class GlobalTableIdPacket extends ServerPacket {
	public Integer tableId;
	public Integer tableGlobalId;
	
	public GlobalTableIdPacket() {
		super(ServerPacket.Type.GLOBAL_TABLE_ID);
	}

	@Override
	public void init(char[] data) {
		int offset = 0;
		this.tableId = Utility.getInt(data, offset);
		this.tableGlobalId = Utility.getInt(data, offset += Integer.SIZE / 8);
	}
}
