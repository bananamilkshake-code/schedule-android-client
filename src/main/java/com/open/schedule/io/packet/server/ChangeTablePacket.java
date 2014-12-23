package com.open.schedule.io.packet.server;

import com.open.schedule.io.packet.ServerPacket;
import com.open.schedule.utility.Utility;

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
		this.time = Utility.getLong(data, offset += Integer.SIZE / 8);
		this.userId = Utility.getInt(data, offset += Long.SIZE / 8);
		Short nameLength = Utility.getShort(data, offset += Integer.SIZE / 8);
		this.name = new String(data, offset += Short.SIZE / 8, nameLength);
		Short descriptionLength = Utility.getShort(data, offset += nameLength);
		this.description = new String(data, offset += Short.SIZE / 8, descriptionLength);
	}
}
