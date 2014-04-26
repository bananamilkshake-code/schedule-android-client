package storage.tables;

import java.util.HashMap;

import utility.Utility;

public class Table extends ChangableData {
	public enum Permission {
		NONE,
		READ,
		WRITE
	};

	private HashMap<Integer, Permission> readers = new HashMap<Integer, Permission>();
	private HashMap<Integer, Task> tasks = new HashMap<Integer, Task>();

	public Table() {
		
	}
	
	public Table(Integer creatorId, Long creationTime, String name, String description) {
		this();
		this.change(new TableInfo(creatorId, creationTime, name, description));
	}
	
	public Table(Integer creatorId, String name, String description) {
		change((Change)(new TableInfo(creatorId, Utility.getUnixTime(), name, description)));
	}

	public void setPermission(int user_id, Permission permission) {
		if (permission == Permission.NONE) {
			readers.remove(user_id);
			return;
		}

		readers.put(user_id, permission);
	}

	public Task addTask(Integer task_id, Task task) {
		return tasks.put(task_id, task);
	}

	public void removeTask(Integer task_id) {
		tasks.remove(task_id);
	}
	
	public Task getTask(Integer task_id) {
		return tasks.get(task_id);
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
				this.name = ((TableInfo)prev).name;
			if (this.description == null)
				this.description = ((TableInfo)prev).description;
		}
	}
	
	public final HashMap<Integer, Permission> getReaders() {
		return readers;
	}
	
	public final HashMap<Integer, Task> getTasks() {
		return tasks;
	}
}
