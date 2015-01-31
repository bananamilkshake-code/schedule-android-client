package com.open.schedule.account;

import com.open.schedule.account.tables.Table;
import com.open.schedule.account.tables.Task;

import java.util.Date;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class Tables {
	private NavigableMap<Integer, Table> tables = new TreeMap<Integer, Table>();

	public Table create(final String name, final String description, final int creatorId) {
		int tableId = this.tables.size();
		Table table = new Table(tableId, name, description, creatorId);

		this.tables.put(tableId, table);

		return table;
	}

	public Task createTask(Integer tableId, final String name, final String description, int creatorId, Date startDate, Date endDate, Date startTime, Date endTime, Short period) {
		Table table = this.tables.get(tableId);
		int taskId = table.getTasks().size();

		Task task = new Task(taskId, tableId, name, description, creatorId, startDate, endDate, startTime, endTime, period);

		tables.get(tableId).addTask(taskId, task);

		return task;
	}

	public final SortedMap<Integer, Table> getTables() {
		return tables;
	}
}
