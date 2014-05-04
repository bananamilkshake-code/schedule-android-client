package io.packet.server;

import utility.Utility;
import io.packet.ServerPacket;

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
		this.taskGlobalId = Utility.getInt(data, offset += Integer.SIZE);
		this.userId = Utility.getInt(data, offset += Integer.SIZE);
		this.time = Utility.getLong(data, offset += Integer.SIZE);
		Short commentLength = Utility.getShort(data, offset += Long.SIZE);
		this.comment = new String(data, offset += Short.SIZE, commentLength);
	}
}
