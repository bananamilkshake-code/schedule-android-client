package table;

import java.util.Map;
import java.util.HashMap;

public class Table extends ChangableData {
	public enum Permission {
		NONE,
		READ,
		WRITE
	};

	private Map<Integer, Permission> readers = new HashMap<Integer, Permission>();
	private Map<Integer, Task> tasks = new HashMap<Integer, Task>();

	public Table() {}
	
	public Table(Integer creatorId, String name, String description) {
		change((Change)(new TableInfo(creatorId, name, description)));
	}

	public void setPermission(int user_id, Permission permission) {
		if (permission == Permission.NONE)
		{
			readers.remove(user_id);
			return;
		}

		readers.put(user_id, permission);
	}

	public void addTask(Integer task_id, Task task) {
		tasks.put(task_id, task);
	}

	public void removeTask(Integer task_id) {
		tasks.remove(task_id);
	}

	public class TableInfo extends Change {
		private String name = null;
		private String description = null;
		
		public TableInfo(Integer creatorId, String name, String description) {
			super(creatorId);
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
}
