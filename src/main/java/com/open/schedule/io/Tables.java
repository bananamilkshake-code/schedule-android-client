package com.open.schedule.io;

import java.util.ArrayList;
import java.util.Date;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.Iterator;
import java.util.TreeMap;

import android.util.Log;
import com.open.schedule.storage.tables.Table;
import com.open.schedule.storage.tables.Task;
import com.open.schedule.storage.tables.Table.Permission;

public class Tables {
	private NavigableMap<Integer, Table> tables = new TreeMap<Integer, Table>();
	private NavigableMap<Integer, Integer> indexId = new TreeMap<Integer, Integer>();
	
	public void createTable(Integer tableId, Table table) {
		tables.put(tableId, table);
	}

	public void createTable(Integer tableId, Integer globalId, Long updateTime) {
		Table table = new Table(tableId, updateTime);
		tables.put(tableId, table);
		updateTableGlobalId(tableId, globalId);
	}
	
	public void createTask(Integer tableId, Integer taskId, Task task) {
		tables.get(tableId).addTask(taskId, task);
	}
	
	public void createTask(Integer tableId, Integer taskId, Integer globalId, Long updateTime) {
		Task task = new Task(taskId, updateTime);
		tables.get(tableId).addTask(taskId, task);
		Integer tableGlobalId = this.findInnerTable(tableId);
		if (tableGlobalId != 0)
			updateTaskGlobalId(tableGlobalId, globalId, taskId);
	}
	
	public void createComment(Integer tableId, Integer taskId, Integer userId, Long time, String comment) {
		tables.get(tableId).getTask(taskId).addComment(userId, time, comment);
	}
	
	public void changeTable(Integer tableId, Integer userId, Long time, String name, String description) {
		Table table = tables.get(tableId);
		table.change(time, table.new TableInfo(userId, time, name, description));
	}
	
	public void changeTask(Integer tableId, Integer taskId, Integer userId, Long time, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime, Integer period) {
		Table table = tables.get(tableId);
		Task task = table.getTask(taskId);
		task.change(time, task.new TaskChange(userId, time, name, description, startDate, endDate, startTime, endTime, period));
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

	public void updateTable(Integer tableId, Long time) {
		tables.get(tableId).update(time);
	}
	
	public void updateTask(Integer tableId, Integer taskId, Long time) {
		tables.get(tableId).getTask(taskId).update(time);
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
	
	public Integer findInnerTable(Integer tableId) {
		return tables.get(tableId).getGlobalId();
	}
	
	public Integer findInnerTask(Integer tableId, Integer taskId) {
		return tables.get(tableId).getTask(taskId).getGlobalId();
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
	
	public final SortedMap<Integer, Table> getTables() {
		return tables;
	}
	
	public ArrayList<Integer> getNewTables() {
		ArrayList<Integer> tableIds = new ArrayList<Integer>();
		Iterator<Integer> tableIter = this.tables.descendingKeySet().iterator();
		while (tableIter.hasNext()) {
			Integer tableId = tableIter.next();
			if (tables.get(tableId).getGlobalId() != null)
				continue;
			tableIds.add(0, tableId);
		}
		return tableIds;
	}
	
	public TreeMap<Integer, ArrayList<Integer>> getNewTasks() {
		TreeMap<Integer, ArrayList<Integer>> tasks = new TreeMap<Integer, ArrayList<Integer>>();
		Iterator<Integer> tableIter = this.tables.descendingKeySet().iterator();
		while (tableIter.hasNext()) {
			Integer tableId = tableIter.next();
			Table table = tables.get(tableId);
			if (table.getGlobalId() == null)
				continue;
			
			Iterator<Integer> taskIter = table.getTasks().descendingKeySet().iterator();
			while (taskIter.hasNext()) {
				Integer taskId = taskIter.next();
				if (table.getTask(taskId).getGlobalId() != null)
					continue;
				
				if (tasks.get(tableId) == null)
					tasks.put(tableId, new ArrayList<Integer>());
				tasks.get(tableId).add(taskId);
			}
		}
		return tasks;
	}
	
	TreeMap<Integer, ArrayList<Long>> getNewTableChanges(Long logoutTime, Integer clientId) {
		TreeMap<Integer, ArrayList<Long>> tableChanges = new TreeMap<Integer, ArrayList<Long>>();
		Iterator<Integer> tableIter = this.tables.descendingKeySet().iterator();
		while (tableIter.hasNext()) {
			Integer tableId = tableIter.next();
			Table table = tables.get(tableId);
			if (table.getGlobalId() == null)
				continue;
			
			ArrayList<Long> changeTimes = table.getNewChanges(clientId);
			if (changeTimes.isEmpty())
				continue;
			tableChanges.put(tableId, changeTimes);
		}
		return tableChanges;
	}

	public SortedMap<Integer, SortedMap<Integer, ArrayList<Long>>> getNewTaskChanges(Long logoutTime, Integer clientId) {
		SortedMap<Integer, SortedMap<Integer, ArrayList<Long>>> tasksChanges = new TreeMap<Integer, SortedMap<Integer, ArrayList<Long>>>();
		Iterator<Integer> tableIter = this.tables.descendingKeySet().iterator();
		while (tableIter.hasNext()) {
			Integer tableId = tableIter.next();
			Table table = tables.get(tableId);
			if (table.getGlobalId() == null)
				continue;
			
			Iterator<Integer> taskIter = table.getTasks().descendingKeySet().iterator();
			while (taskIter.hasNext()) {
				Integer taskId = taskIter.next();
				Task task = table.getTask(taskId);
				if (task.getGlobalId() == null)
					continue;
				
				ArrayList<Long> changeTimes = task.getNewChanges(clientId);
				if (changeTimes.isEmpty())
					continue;
				
				if (tasksChanges.get(tableId) == null)
					tasksChanges.put(tableId, new TreeMap<Integer, ArrayList<Long>>());
				if (tasksChanges.get(tableId).get(taskId) == null)
					tasksChanges.get(tableId).put(taskId, changeTimes);
			}
		}
		return tasksChanges;
	}
	
	public SortedMap<Integer, SortedMap<Integer, ArrayList<Long>>> getNewComments(Long logoutTime, Integer clientId) {
		SortedMap<Integer, SortedMap<Integer, ArrayList<Long>>> comments = new TreeMap<Integer, SortedMap<Integer, ArrayList<Long>>>();
		Iterator<Integer> tableIter = this.tables.descendingKeySet().iterator();
		while (tableIter.hasNext()) {
			Integer tableId = tableIter.next();
			Table table = tables.get(tableId);
			if (table.getGlobalId() == null)
				continue;
			
			Iterator<Integer> taskIter = table.getTasks().descendingKeySet().iterator();
			while (taskIter.hasNext()) {
				Integer taskId = taskIter.next();
				Task task = table.getTask(taskId);
				if (task.getGlobalId() == null)
					continue;
				
				ArrayList<Long> commentTimes = task.getNewComments(logoutTime, clientId);
				if (commentTimes.isEmpty())
					continue;
					
				if (comments.get(tableId) == null)
					comments.put(tableId, new TreeMap<Integer, ArrayList<Long>>());
				if (comments.get(tableId).get(taskId) == null)
					comments.get(tableId).put(taskId, commentTimes);
			}
		}
		return comments;
	}
}
