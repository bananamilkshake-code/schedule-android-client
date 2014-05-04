package io.packet.server;

import io.packet.ServerPacket;
import io.packet.ServerPacket.Type;
import utility.Utility;

public class ChangeTablePacket extends ServerPacket {
	public Integer tableGlobalId;
	public Long time;
	public Integer userId;
	public String name;
	public String description;
	
	public ChangeTablePacket() {
		super(ServerPacket.Type.CHANGE_TABLE);
	}

	@Override
	public void init(char[] data) {
		int offset = 0;
		this.tableGlobalId = Utility.getInt(data, offset);
		this.time = Utility.getLong(data, offset += Integer.SIZE);
		this.userId = Utility.getInt(data, offset += Long.SIZE);
		Short nameLength = Utility.getShort(data, offset += Integer.SIZE);
		this.name = new String(data, offset += Short.SIZE, nameLength);
		Short descriptionLength = Utility.getShort(data, offset += nameLength);
		this.description = new String(data, offset += Short.SIZE, descriptionLength);
	}
}
