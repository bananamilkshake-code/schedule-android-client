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

	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

	public Database(Context context) {
		this.dbHelper = new DatabaseHelper(context);
		this.database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void loadTables(HashMap<Integer, Table> tables) {
		String[] columns = {DatabaseHelper.INNER_ID};
		Cursor cursorTables = database.query(DatabaseHelper.TABLE_TABLES, columns, null, null, null, null, null);

		if (cursorTables.moveToFirst()) {
			int id = cursorTables.getColumnIndex(DatabaseHelper.INNER_ID);
			while(!cursorTables.isAfterLast()) {
				Integer idVal = cursorTables.getInt(id);
				tables.put(idVal, new Table());
				cursorTables.moveToNext();
			}
		}
		cursorTables.close();

		String[] columnsChanges = {DatabaseHelper.TABLE_ID, DatabaseHelper.TIME, DatabaseHelper.USER_ID, 
				DatabaseHelper.CHANGE_NAME, DatabaseHelper.CHANGE_DESCRIPTION};
		Cursor cursorChanges = database.query(DatabaseHelper.TABLE_TABLE_CHANGES, columnsChanges, null, null, null, null, null);

		if (cursorChanges.moveToFirst()) {
			int tableId = cursorChanges.getColumnIndex(DatabaseHelper.TABLE_ID);
			int time = cursorChanges.getColumnIndex(DatabaseHelper.TIME);
			int userId = cursorChanges.getColumnIndex(DatabaseHelper.USER_ID);
			int name = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_NAME);
			int description = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_DESCRIPTION);

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

	private void loadTasks(HashMap<Integer, Table> tables) {
		String[] columnsChanges = {DatabaseHelper.TABLE_ID, DatabaseHelper.TASK_ID, DatabaseHelper.TIME, DatabaseHelper.USER_ID, 
				DatabaseHelper.CHANGE_NAME, DatabaseHelper.CHANGE_DESCRIPTION, 
				DatabaseHelper.CHANGE_TASK_START_DATE, DatabaseHelper.CHANGE_TASK_END_DATE, 
				DatabaseHelper.CHANGE_TASK_START_TIME, DatabaseHelper.CHANGE_TASK_END_TIME};
		Cursor cursorChanges = database.query(DatabaseHelper.TABLE_TASK_CHANGES, columnsChanges, null, null, null, null, null);

		if (cursorChanges.moveToFirst()) {
			int tableId = cursorChanges.getColumnIndex(DatabaseHelper.TABLE_ID);
			int taskId = cursorChanges.getColumnIndex(DatabaseHelper.TASK_ID);
			int time = cursorChanges.getColumnIndex(DatabaseHelper.TIME);
			int userId = cursorChanges.getColumnIndex(DatabaseHelper.USER_ID);
			int name = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_NAME);
			int description = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_DESCRIPTION);
			int startDate = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_TASK_START_DATE);
			int endDate = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_TASK_END_DATE);
			int startTime = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_TASK_START_TIME);
			int endTime = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_TASK_END_TIME);

			while(!cursorChanges.isAfterLast()) {
				Integer tableIdVal = cursorChanges.getInt(tableId);
				Integer taskIdVal = cursorChanges.getInt(taskId);
				Task task = tables.get(tableIdVal).getTask(taskIdVal);
				if (task == null)
					task = tables.get(tableIdVal).addTask(taskIdVal, new Task());

				Integer userIdVal = cursorChanges.getInt(userId);
				Long timeVal = cursorChanges.getLong(time);
				String nameVal = cursorChanges.getString(name);
				String descVal = cursorChanges.getString(description);
				Date startDateVal = null;
				Date endDateVal = null;
				Date startTimeVal = null;
				Date endTimeVal = null;

				try {
					String val = cursorChanges.getString(startDate);
					if (val != null)
						startDateVal = dateFormatter.parse(val);
					
					val = cursorChanges.getString(endDate);
					if (val != null)
						endDateVal = dateFormatter.parse(val);
					
					val = cursorChanges.getString(startTime);
					if (val != null)
						startTimeVal = timeFormatter.parse(val);
					
					val = cursorChanges.getString(endTime);
					if (val != null)
						endTimeVal = timeFormatter.parse(val);
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
		String[] columns = {DatabaseHelper.TABLE_ID, DatabaseHelper.TASK_ID, DatabaseHelper.TIME, DatabaseHelper.USER_ID, DatabaseHelper.COMMENTS_TEXT};
		Cursor cursor = database.query(DatabaseHelper.TABLE_COMMENTS, columns, null, null, null, null, DatabaseHelper.TABLE_ID + ", " + DatabaseHelper.TASK_ID + ", " + DatabaseHelper.COMMENTS_TEXT);

		int tableId = cursor.getColumnIndex(DatabaseHelper.TABLE_ID);
		int taskId = cursor.getColumnIndex(DatabaseHelper.TASK_ID);
		int time = cursor.getColumnIndex(DatabaseHelper.TIME);
		int userId = cursor.getColumnIndex(DatabaseHelper.USER_ID);
		int textId = cursor.getColumnIndex(DatabaseHelper.COMMENTS_TEXT);

		if (cursor.moveToFirst()) {
			while(!cursor.isAfterLast()) {
				Integer tableIdVal = cursor.getInt(tableId);
				Integer taskIdVal = cursor.getInt(taskId);
				Task task = tables.get(tableIdVal).getTask(taskIdVal);
				if (task != null) {
					Integer commentatorVal = cursor.getInt(userId);
					Long timeVal = cursor.getLong(time);
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
		values.put(DatabaseHelper.TABLE_ID, tableId);
		Integer taskId = (int) database.insert(DatabaseHelper.TABLE_TASKS, null, values);

		Entry<Long, Change> firstChange = task.getInitial();
		Long time = (Long) firstChange.getKey();
		Task.TaskChange change = (TaskChange) firstChange.getValue();

		changeTask(userId, tableId, taskId, change, time);

		return taskId;
	}

	public void createComment(Integer tableId, Integer taskId, Long time, Integer userId, String text) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.TABLE_ID, tableId);
		values.put(DatabaseHelper.TASK_ID, taskId);
		values.put(DatabaseHelper.USER_ID, userId);
		values.put(DatabaseHelper.TIME, time);
		values.put(DatabaseHelper.COMMENTS_TEXT, text);
		Long res = database.insert(DatabaseHelper.TABLE_COMMENTS, null, values);
		Log.d("Comment creation", res.toString());
	}
	
	public void changeTable(Integer userId, Integer tableId, Table.TableInfo change, Long time) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.USER_ID, userId);
		values.put(DatabaseHelper.TABLE_ID, tableId);
		values.put(DatabaseHelper.TIME, time);
		values.put(DatabaseHelper.CHANGE_NAME, change.name);
		values.put(DatabaseHelper.CHANGE_DESCRIPTION, change.description);
		Long res = database.insert(DatabaseHelper.TABLE_TABLE_CHANGES, null, values);
		Log.d("ChangeTable", res.toString());
	}

	public void changeTask(Integer userId, Integer tableId, Integer taskId, Task.TaskChange change,Long time) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.USER_ID, userId);
		values.put(DatabaseHelper.TABLE_ID, tableId);
		values.put(DatabaseHelper.TASK_ID, taskId);
		values.put(DatabaseHelper.TIME, time);
		values.put(DatabaseHelper.CHANGE_NAME, change.name);
		values.put(DatabaseHelper.CHANGE_DESCRIPTION, change.description);
		values.put(DatabaseHelper.CHANGE_TASK_START_DATE, dateFormatter.format(change.startDate));
		values.put(DatabaseHelper.CHANGE_TASK_END_DATE, dateFormatter.format(change.endDate));
		values.put(DatabaseHelper.CHANGE_TASK_START_TIME, timeFormatter.format(change.startTime));
		values.put(DatabaseHelper.CHANGE_TASK_END_TIME, timeFormatter.format(change.endTime));
		Long res = database.insert(DatabaseHelper.TABLE_TASK_CHANGES, null, values);
		Log.d("ChangeTask", res.toString());
	}

	public void updateTableGlobalId(Integer tableId, Integer tableGlobalId) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.GLOBAL_ID, tableGlobalId);
		database.update(DatabaseHelper.TABLE_TABLES, values, DatabaseHelper.INNER_ID + " =?", new String[] {tableId.toString()});
	}

	public void updateTaskGlobalId(Integer taskId, Integer tableGlobalId, Integer taskGlobalId) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.GLOBAL_ID, tableGlobalId);
		String columns[] = {DatabaseHelper.INNER_ID};
		String whereArg = DatabaseHelper.GLOBAL_ID + " = " + tableGlobalId.toString();
		Cursor cursor;

		cursor = database.query(DatabaseHelper.TABLE_TABLES, columns, whereArg, null, null, null, null);
		if (!cursor.moveToFirst()) {
			cursor.close();
			return;
		}

		Integer tableId = 0;
		while(!cursor.isAfterLast()) 
			tableId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.INNER_ID));
		cursor.close();

		values = new ContentValues();
		values.put(DatabaseHelper.GLOBAL_ID, taskGlobalId);
		database.update(DatabaseHelper.TABLE_TASKS, values, DatabaseHelper.TABLE_ID + " =? AND " + DatabaseHelper.INNER_ID + " = ?", new String[] {tableId.toString(), taskId.toString()});
	}

	public void setPermission(Integer table_id, Integer user_id, Table.Permission permission) {
		if (permission == Permission.NONE) {
			database.delete(DatabaseHelper.TABLE_READERS, DatabaseHelper.USER_ID + " = ?", new String[] {user_id.toString()});
		}
		else {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.TABLE_ID, table_id);
			values.put(DatabaseHelper.USER_ID, user_id);
			database.replace(DatabaseHelper.TABLE_READERS, null, values);
		}
	}

	public void newUser(Integer user_id, String name) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.USER_ID, user_id);
		values.put(DatabaseHelper.USERS_NAME, name);
		database.insert(DatabaseHelper.TABLE_USERS, null, values);
	}
	
	public void updateUserId(Integer userId) {
		ContentValues userValues = new ContentValues();
		userValues.put(DatabaseHelper.GLOBAL_ID, userId);
		database.update(DatabaseHelper.TABLE_USERS, userValues, DatabaseHelper.GLOBAL_ID + " =?", new String[] {userId.toString()});

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.USER_ID, userId);
		String tablesToUpdate[] = {DatabaseHelper.TABLE_TABLE_CHANGES, DatabaseHelper.TABLE_TASK_CHANGES, DatabaseHelper.TABLE_READERS, DatabaseHelper.TABLE_COMMENTS};
		for (String table : tablesToUpdate) {
			database.update(table, values, DatabaseHelper.USER_ID + " =?", new String[] {userId.toString()});
		}
	}
}
