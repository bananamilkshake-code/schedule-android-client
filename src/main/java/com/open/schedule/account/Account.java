package com.open.schedule.account;

import com.open.schedule.account.tables.Plans;
import com.open.schedule.account.tables.Table;
import com.open.schedule.account.tables.Task;
import com.open.schedule.io.Client;
import com.open.schedule.io.Tables;

import java.util.Date;
import java.util.SortedMap;

public class Account {
	private int id = 0;
	private long lastSyncTime = 0;

	private final Tables tables = new Tables();

	private Client client = null;

	public Account() {}

	public void setClient(final Client client) {
		this.client = client;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getLastSyncTime() {
		return this.lastSyncTime;
	}

	public int createTable(String name, String description, int creatorId, boolean local) {
		Table table = this.tables.create(name, description, creatorId);

		if (local)
			this.client.sync(table);

		return table.getId();
	}

	public void createTask(int tableId, String name, String description, int creatorId, Date startDate, Date endDate, Date startTime, Date endTime, int period, boolean local) {
		Task task = this.tables.createTask(tableId, name, description, creatorId, startDate, endDate, startTime, endTime, period);

		if (local)
			this.client.sync(task);
	}

	public SortedMap<Integer, Table> getTables() {
		return this.tables.getTables();
	}

	public Plans getTablePlans() {
		return new Plans(this.tables);
	}
}
