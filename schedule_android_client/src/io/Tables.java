package io;

import java.util.Date;
import java.util.HashMap;

import android.util.Log;
import storage.tables.Table;
import storage.tables.Task;
import storage.tables.Table.Permission;

public class Tables {
	private HashMap<Integer, Table> tables = new HashMap<Integer, Table>();
	private HashMap<Integer, Integer> indexId = new HashMap<Integer, Integer>();

	public void createTable(Integer tableId, Table table) {
		tables.put(tableId, table);
	}

	public void createTask(Integer tableId, Integer taskId, Task task) {
		tables.get(tableId).addTask(taskId, task);
	}
	
	public void createComment(Integer tableId, Integer taskId, Integer userId, Long time, String comment) {
		tables.get(tableId).getTask(taskId).addComment(userId, time, comment);
	}
	
	public void changeTable(Integer tableId, Integer userId, Long time, String name, String description) {
		Table table = tables.get(tableId);
		table.change(time, table.new TableInfo(userId, time, name, description));
	}
	
	public void changeTask(Integer tableId, Integer taskId, Integer userId, Long time, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime) {
		Table table = tables.get(tableId);
		Task task = table.getTask(taskId);
		task.change(time, task.new TaskChange(userId, time, name, description, startDate, endDate, startTime, endTime));
	}
	
	public void changePermission(Integer tableId, Integer userId, Permission permission) {
		tables.get(tableId).setPermission(userId, permission);
	}
	
	public void updateTableGlobalId(Integer tableId, Integer tableGlobalId) {
		tables.get(tableId).updateglobalId(tableGlobalId);
		indexId.put(tableGlobalId, tableId);
	}

	public void updateTaskGlobalId(Integer tableGlobalId, Integer taskGlobalId, Integer taskId) {
		Integer tableId = indexId.get(tableGlobalId);
		Table table = tables.get(tableId);
		table.updateTaskGlobalId(taskId, taskGlobalId);
	}
	
	public Integer findGlobalTable(Integer tableGlobalId) {
		return indexId.get(tableGlobalId);
	}
	
	public Integer findGlobalTask(Integer tableId, Integer taskGlobalId) {
		Table table = tables.get(tableId);
		if (table == null) {
			Log.w("Tables", "findGlobalTask found null table with inner id " + tableId);
			return null;
		}
		return table.getTaskId(taskGlobalId);
	}
	
	public void changeTableId(Integer tableGlobalId) {
		tableGlobalId = indexId.get(tableGlobalId);
	}
	
	public void changeTaskId(Integer tableId, Integer taskGlobalId) {
		Table table = tables.get(tableId);
		if (table == null) {
			Log.w("Tables", "changeTaskId found null table with inner id " + tableId);
			return;
		}
		
		taskGlobalId = table.getTaskId(taskGlobalId);
	}
	
	public final HashMap<Integer, Table> getTables() {
		return tables;
	}
}
