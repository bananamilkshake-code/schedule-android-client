package storage.tables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import storage.tables.Task.TaskChange;
import utility.Utility;
import io.Tables;

public class Plans {
	private final Tables tables;
	private ArrayList<TablePlan> todayPlans = new ArrayList<TablePlan>();
	
	public class TablePlan {
		public Table table;
		public ArrayList<Task> tasks;

		private TablePlan(Table table, ArrayList<Task> tasks) {
			this.table = table;
			this.tasks = tasks;
		}
	}

	public Plans(Tables tables) {
		this.tables = tables;
		update();
	}
	
	public TablePlan getTodayPlan(int id) {
		return this.todayPlans.get(id);
	}

	public void update() {
		Iterator<Entry<Integer, Table>> tableIter = tables.getTables().entrySet().iterator();
		while (tableIter.hasNext()) {
			Entry<Integer, Table> entry = tableIter.next();
			Table table = entry.getValue();
			ArrayList<Task> tasks = getTodayTasks(table);
			if (!tasks.isEmpty()) {
				this.todayPlans.add(new TablePlan(table, tasks));
			}
		}
	}

	static private ArrayList<Task> getTodayTasks(Table table) {
		ArrayList<Task> tasks = new ArrayList<Task>();
		Iterator<Entry<Integer, Task>> iterator = table.getTasks().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Task> entry = iterator.next();
			Long currentTime = Utility.getUnixTime();
			Task task = entry.getValue();
			TaskChange data = (TaskChange) task.getData();
			if (data.endDate.getTime() < currentTime * 1000 || data.startDate.getTime() > currentTime * 1000)
				continue;
			if ((currentTime - data.startDate.getTime()) % data.period == 0)
				tasks.add(task);
		}
		return tasks;
	}

	public int count() {
		return todayPlans.size();
	}
}
