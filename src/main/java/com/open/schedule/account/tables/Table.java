package com.open.schedule.account.tables;

import com.open.schedule.utility.Utility;

import java.util.NavigableMap;
import java.util.TreeMap;

public class Table extends ChangeableData {
	private NavigableMap<Integer, Task> tasks = new TreeMap<Integer, Task>();

	public Table(int id, final String name, final String description, int creatorId) {
		super(id);

		long currentTime = Utility.getUnixTime();
		this.change(currentTime, new TableChange(creatorId, currentTime, name, description));
	}

	public Task addTask(Integer taskId, Task task) {
		tasks.put(taskId, task);
		return tasks.get(taskId);
	}

	public Task getTask(Integer taskId) {
		return tasks.get(taskId);
	}

	public final NavigableMap<Integer, Task> getTasks() {
		return tasks;
	}

	public class TableChange extends Change {
		public String name;
		public String description;

		public TableChange(Integer creatorId, long time, String name, String description) {
			super(creatorId, time);
			this.name = name;
			this.description = description;
		}

		public Boolean hasNulls() {
			return (name == null || description == null);
		}

		public void merge(Change prev) {
			if (this.name == null)
				this.name = ((TableChange) prev).name;
			if (this.description == null)
				this.description = ((TableChange) prev).description;
		}
	}
}
