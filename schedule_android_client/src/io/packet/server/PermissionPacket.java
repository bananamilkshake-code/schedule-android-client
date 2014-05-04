package io.packet.server;

import utility.Utility;
import io.packet.ServerPacket;

public class PermissionPacket extends ServerPacket {
	public Integer userId;
	public Integer tableGlobalId;
	public byte permission;

	public PermissionPacket() {
		super(ServerPacket.Type.PERMISSION);
	}

	@Override
	public void init(char[] data) {
		Integer offset = 0;
		this.userId = Utility.getInt(data, offset);
		this.tableGlobalId = Utility.getInt(data, offset += Integer.SIZE);
		this.permission = Utility.getByte(data, offset += Integer.SIZE);
	}
}
