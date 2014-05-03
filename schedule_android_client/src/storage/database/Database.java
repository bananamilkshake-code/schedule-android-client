package storage.database;

import java.util.Date;
import java.util.Map.Entry;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import storage.tables.*;
import storage.tables.ChangableData.Change;
import storage.tables.Table.Permission;
import storage.tables.Table.TableInfo;
import storage.tables.Task.TaskChange;
import utility.Utility;

public class Database {
	private DatabaseHelper dbHelper;
	private SQLiteDatabase database;
	
	public Database(Context context) {
		this.dbHelper = new DatabaseHelper(context);
		this.database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public void loadTables(HashMap<Integer, Table> tables) {
		String[] columns = {"_id"};
		Cursor cursorTables = database.query(DatabaseHelper.TABLE_TABLES, columns, null, null, null, null, null);

		if (cursorTables.moveToFirst()) {
			int id = cursorTables.getColumnIndex("_id");
			while(!cursorTables.isAfterLast()) {
				Integer idVal = cursorTables.getInt(id);
				tables.put(idVal, new Table());
				cursorTables.moveToNext();
			}
		}
		cursorTables.close();

		String[] columnsChanges = {"table_id", "time", "user_id", "name", "description"};
		Cursor cursorChanges = database.query(DatabaseHelper.TABLE_TABLE_CHANGES, columnsChanges, null, null, null, null, null);

		if (cursorChanges.moveToFirst()) {
			int tableId = cursorChanges.getColumnIndex("table_id");
			int time = cursorChanges.getColumnIndex("time");
			int userId = cursorChanges.getColumnIndex("user_id");
			int name = cursorChanges.getColumnIndex("name");
			int description = cursorChanges.getColumnIndex("description");

			while(!cursorChanges.isAfterLast()) {
				Integer tableIdVal = cursorChanges.getInt(tableId);
				
				Table table = tables.get(tableIdVal);
				if (table != null) {
					Integer userIdVal = cursorChanges.getInt(userId);
					Long timeVal = cursorChanges.getLong(time);
					String nameVal = cursorChanges.getString(name);
					String descriptionVal = cursorChanges.getString(description);
					table.change(timeVal, table.new TableInfo(userIdVal, timeVal, nameVal, descriptionVal));
				}
				cursorChanges.moveToNext();
			}
		}
		cursorChanges.close();
		
		loadTasks(tables);
	}

	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

	private final String startDateColName = "strftime(\'%Y-%m-%d\', start_date)";
	private final String endDateColName = "strftime(\'%Y-%m-%d\', end_date)";
	private final String startTimeColName = "strftime(\'%H:M:%S\', start_time)";
	private final String endTimeColName = "strftime(\'%H:M:%S\', end_time)";
	
	private void loadTasks(HashMap<Integer, Table> tables) {
		String[] columnsChanges = {"task_id", "table_id", "time", "user_id", "name", 
				"description", startDateColName, endDateColName, startTimeColName, endTimeColName};
		Cursor cursorChanges = database.query(DatabaseHelper.TABLE_TASK_CHANGES, columnsChanges, null, null, null, null, null);

		if (cursorChanges.moveToFirst()) {
			int table_id = cursorChanges.getColumnIndex("table_id");
			int task_id = cursorChanges.getColumnIndex("task_id");
			int time = cursorChanges.getColumnIndex("time");
			int user_id = cursorChanges.getColumnIndex("user_id");
			int name = cursorChanges.getColumnIndex("name");
			int description = cursorChanges.getColumnIndex("description");
			int startDate = cursorChanges.getColumnIndex(startDateColName);
			int endDate = cursorChanges.getColumnIndex(endDateColName);
			int startTime = cursorChanges.getColumnIndex(startTimeColName);
			int endTime = cursorChanges.getColumnIndex(endTimeColName);

			while(!cursorChanges.isAfterLast()) {
				Integer tableIdVal = cursorChanges.getInt(table_id);
				Integer taskIdVal = cursorChanges.getInt(task_id);
				Task task = tables.get(table_id).getTask(taskIdVal);
				if (task == null)
					task = tables.get(tableIdVal).addTask(taskIdVal, new Task());

				Integer userIdVal = cursorChanges.getInt(user_id);
				Long timeVal = cursorChanges.getLong(time);
				String nameVal = cursorChanges.getString(name);
				String descVal = cursorChanges.getString(description);
				Date startDateVal = null;
				Date endDateVal = null;
				Date startTimeVal = null;
				Date endTimeVal = null;

				try {
					String val = cursorChanges.getString(startDate);
					if (val == null)
						startDateVal = dateFormatter.parse("2014-05-05");
					
					val = cursorChanges.getString(endDate);
					if (val == null)
						endDateVal = dateFormatter.parse("2014-05-05");
					
					val = cursorChanges.getString(startTime);
					if (val == null)
						startTimeVal = timeFormatter.parse("11:12:00");
					
					val = cursorChanges.getString(endTime);
					if (val == null)
						endTimeVal = timeFormatter.parse("11:12:00");
				} catch (ParseException e) {
					Log.w(Database.class.getName(), "Date task changes parsing", e);
				}

				task.change(timeVal, task.new TaskChange(userIdVal, timeVal, nameVal, descVal, startDateVal, endDateVal, startTimeVal, endTimeVal));
				cursorChanges.moveToNext();
			}
		}
		cursorChanges.close();

		loadComments(tables);
	}
	
	public void loadComments(HashMap<Integer, Table> tables) {
		String[] columns = {"table_id", "task_id", "commentator_id", "time", "commentary"};
		Cursor cursor = database.query(DatabaseHelper.TABLE_COMMENTS, columns, null, null, null, null, "table_id, task_id, time");

		int tableId = cursor.getColumnIndex("table_id");
		int taskId = cursor.getColumnIndex("task_id");
		int timeId = cursor.getColumnIndex("time");
		int commentatorId = cursor.getColumnIndex("commentator_id");
		int textId = cursor.getColumnIndex("text");
		
		if (cursor.moveToFirst()) {
			while(!cursor.isAfterLast()) {
				Integer tableIdVal = cursor.getInt(tableId);
				Integer taskIdVal = cursor.getInt(taskId);
				Task task = tables.get(tableIdVal).getTask(taskIdVal);
				if (task != null) {
					Integer commentatorVal = cursor.getInt(commentatorId);
					Long timeVal = cursor.getLong(timeId);
					String textVal = cursor.getString(textId);
					task.addComment(commentatorVal, timeVal, textVal);
				}
				cursor.moveToNext();
			}
		}
		cursor.close();
	}
	
	public Integer createTable(Integer userId, Table table) {
		ContentValues values = new ContentValues();
		values.put("last_update", Utility.getUnixTime());
		Integer tableId = (int) database.insert(DatabaseHelper.TABLE_TABLES, null, values);

		Entry<Long, Change> firstChange = table.getInitial();
		Long time = (Long) firstChange.getKey();
		Table.TableInfo change = (TableInfo) firstChange.getValue();
		changeTable(userId, tableId, change, time);

		return tableId;
	}
	
	public Integer createTask(Integer userId, Integer tableId, Task task) {
		ContentValues values = new ContentValues();
		values.put("table_id", tableId);
		Integer taskId = (int) database.insert(DatabaseHelper.TABLE_TASKS, null, values);

		Entry<Long, Change> firstChange = task.getInitial();
		Long time = (Long) firstChange.getKey();
		Task.TaskChange change = (TaskChange) firstChange.getValue();

		changeTask(userId, tableId, taskId, change, time);

		return taskId;
	}
	
	public void changeTable(Integer userId, Integer tableId, Table.TableInfo change, Long time) {
		ContentValues values = new ContentValues();
		values.put("user_id", userId);
		values.put("table_id", tableId);
		values.put("time", time);
		values.put("name", change.name);
		values.put("description", change.description);
		Long res = database.insert(DatabaseHelper.TABLE_TABLE_CHANGES, null, values);
		Log.d("ChangeTable", res.toString());
	}

	public void changeTask(Integer userId, Integer tableId, Integer taskId, Task.TaskChange change,Long time) {
		ContentValues values = new ContentValues();
		values.put("user_id", userId);
		values.put("table_id", tableId);
		values.put("task_id", taskId);
		values.put("time", time);
		values.put("name", change.name);
		values.put("description", change.description);
		String val = change.startDate.toString();
		values.put("start_time", val);
		values.put("end_date", change.endDate.toString());
		values.put("start_time", change.startTime.toString());
		values.put("end_time", change.endTime.toString());
		Long res = database.insert(DatabaseHelper.TABLE_TASK_CHANGES, null, values);
		Log.d("ChangeTask", res.toString());
	}

	public void setPermission(Integer table_id, Integer user_id, Table.Permission permission) {
		if (permission == Permission.NONE) {
			database.delete(DatabaseHelper.TABLE_READERS, "user_id = ?", new String[] {user_id.toString()});
		}
		else {
			ContentValues values = new ContentValues();
			values.put("table_id", table_id);
			values.put("user_id", user_id);
			database.replace(DatabaseHelper.TABLE_READERS, null, values);
		}
	}

	public void newUser(Integer user_id, String name) {
		ContentValues values = new ContentValues();
		values.put("user_id", user_id);
		values.put("name", name);
		database.insert(DatabaseHelper.TABLE_USERS, null, values);
	}
}
