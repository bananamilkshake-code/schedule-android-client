package com.open.schedule.io.packet.server;

import com.open.schedule.utility.Utility;
import com.open.schedule.io.packet.ServerPacket;

public class CommentaryPacket extends ServerPacket {
	public Integer tableGlobalId;
	public Integer taskGlobalId;
	public Integer userId;
	public Long time;
	public String comment;
	
	public CommentaryPacket() {
		super(ServerPacket.Type.COMMENTARY);
	}
	

	@Override
	public void init(char[] data) {
		int offset = 0;
		this.tableGlobalId = Utility.getInt(data, offset);
		this.taskGlobalId = Utility.getInt(data, offset += Integer.SIZE / 8);
		this.time = Utility.getLong(data, offset += Integer.SIZE / 8);
		this.userId = Utility.getInt(data, offset += Long.SIZE / 8);
		Short commentLength = Utility.getShort(data, offset += Long.SIZE / 8);
		this.comment = new String(data, offset += Short.SIZE / 8, commentLength);
	}
}
