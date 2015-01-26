package com.open.schedule.io.packet.server;

import com.open.schedule.io.packet.ServerPacket;

public class TablePacket extends ServerPacket {
	public int tableGlobalId;
	public long time;
	public int creatorId;

	public String name;
	public String description;

	public TablePacket(byte[] data) {
		super(ServerPacket.Type.TABLE, data);
	}

	@Override
	public void init() {
		this.tableGlobalId = this.getInt();
		this.time = this.getLong();
		this.creatorId = this.getInt();

		this.name = this.getString();
		this.description = this.getString();
	}
}
