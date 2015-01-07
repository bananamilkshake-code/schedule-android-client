package com.open.schedule.storage.tables;

import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class Table extends ChangeableData {
	private Map<Integer, Permission> readers = new TreeMap<Integer, Permission>();

	;
	private NavigableMap<Integer, Task> tasks = new TreeMap<Integer, Task>();
	private SortedMap<Integer, Integer> indexId = new TreeMap<Integer, Integer>();
	public Table(Integer id, Long updatedTime) {
		super(id, updatedTime);
	}

	public Table(Integer id, Integer creatorId, Long creationTime, String name, String description) {
		super(id);
		this.change(creationTime, new TableInfo(creatorId, creationTime, name, description));
	}

	public Table(Integer id, Long time, Integer creatorId, String name, String description) {
		super(id);
		this.change(time, (Change) (new TableInfo(creatorId, time, name, description)));
	}

	public void setPermission(int user_id, Permission permission) {
		if (permission == Permission.NONE) {
			readers.remove(user_id);
			return;
		}

		readers.put(user_id, permission);
	}

	public Task addTask(Integer taskId, Task task) {
		tasks.put(taskId, task);
		return tasks.get(taskId);
	}

	public void removeTask(Integer taskId) {
		tasks.remove(taskId);
	}

	public Task getTask(Integer taskId) {
		return tasks.get(taskId);
	}

	public void updateTaskGlobalId(Integer taskId, Integer taskGlobalId) {
		tasks.get(taskId).updateGlobalId(taskGlobalId);
		indexId.put(taskGlobalId, taskId);
	}

	public Integer getTaskId(Integer taskGlobalId) {
		return indexId.get(taskGlobalId);
	}

	public final Map<Integer, Permission> getReaders() {
		return readers;
	}

	public final NavigableMap<Integer, Task> getTasks() {
		return tasks;
	}

	public enum Permission {
		NONE,
		READ,
		WRITE
	}

	public class TableInfo extends Change {
		public String name;
		public String description;

		public TableInfo(Integer creatorId, Long creationTime, String name, String description) {
			super(creatorId, creationTime);
			this.name = name;
			this.description = description;
		}

		public Boolean hasNulls() {
			return (name == null || description == null);
		}

		public void merge(Change prev) {
			if (this.name == null)
				this.name = ((TableInfo) prev).name;
			if (this.description == null)
				this.description = ((TableInfo) prev).description;
		}
	}
}
