package io.packet.client;

import io.packet.ClientPacket;

import java.io.IOException;

public class PermissionPacket extends ClientPacket {
	public PermissionPacket(Integer userId, Integer tableId, Byte permission) throws IOException {
		super(ClientPacket.Type.PERMISSION);
		this.writeInt(userId);
		this.writeInt(tableId);
		this.writeByte(permission);
	}
}
