package io.packet.server;

import utility.Utility;
import io.packet.ServerPacket;

public class UserPacket extends ServerPacket {
	public Integer userId;
	public String name;
	
	public UserPacket() {
		super(ServerPacket.Type.USER);
	}

	@Override
	public void init(char[] data) {
		Integer offset = 0;
		this.userId = Utility.getInt(data, offset);
		Short nameLength = Utility.getShort(data, offset += Integer.SIZE / 8);
		this.name = new String(data, offset += Short.SIZE / 8, nameLength);
	}
}
