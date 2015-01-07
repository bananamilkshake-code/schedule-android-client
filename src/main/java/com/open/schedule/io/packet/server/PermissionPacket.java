package com.open.schedule.io.packet.server;

import com.open.schedule.utility.Utility;
import com.open.schedule.io.packet.ServerPacket;

public class PermissionPacket extends ServerPacket {
	public Integer userId;
	public Integer tableGlobalId;
	public byte permission;

	public PermissionPacket() {
		super(ServerPacket.Type.PERMISSION);
	}

	@Override
	public void init(byte[] data) {
		Integer offset = 0;
		this.tableGlobalId = Utility.getInt(data, offset);
		this.userId = Utility.getInt(data, offset += Integer.SIZE / 8);
		this.permission = Utility.getByte(data, offset += Integer.SIZE / 8);
	}
}
