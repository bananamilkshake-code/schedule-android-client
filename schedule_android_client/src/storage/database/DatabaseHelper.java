package storage.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{
	public static final String DATABASE_NAME = "schedule";
	public static final String TABLE_USERS = "users";
	public static final String USERS_NAME = "name";

	public static final String TABLE_TABLES = "tables";
	public static final String TABLE_TASKS = "tasks";
	public static final String INNER_ID = "_id";
	public static final String GLOBAL_ID = "id";
	public static final String UPDATE_TIME = "update_time";
	
	public static final String TABLE_ID = "table_id";
	public static final String TASK_ID = "task_id";
	public static final String USER_ID = "user_id";
	public static final String TIME = "time";

	public static final String TABLE_TABLE_CHANGES = "table_changes";
	public static final String TABLE_TASK_CHANGES = "task_changes";
	public static final String CHANGE_NAME = "name";
	public static final String CHANGE_DESCRIPTION = "description";
	public static final String CHANGE_TASK_START_DATE = "start_date";
	public static final String CHANGE_TASK_END_DATE = "end_date";
	public static final String CHANGE_TASK_START_TIME = "start_time";
	public static final String CHANGE_TASK_END_TIME = "end_time";	
	
	public static final String TABLE_COMMENTS = "comments";
	public static final String COMMENTS_TEXT = "commentary";
	public static final String TABLE_READERS = "readers";
	public static final String READERS_PERMISSION = "permission";

	private static final int DATABASE_VERSION = 1;

	private static final String CREATE_USERS = 
			"CREATE TABLE " + TABLE_USERS + " (" +
					GLOBAL_ID + " INTEGER PRIMARY KEY," +
					USERS_NAME + " VARCHAR(50) NOT NULL," +
					"UNIQUE(" + GLOBAL_ID + ") " +
				")";

	private static final String CREATE_TABLES = 
			"CREATE TABLE " + TABLE_TABLES + " (" +
					INNER_ID + " INTEGER PRIMARY KEY," +
					GLOBAL_ID + " INT(10)," +
					UPDATE_TIME + " INT(10) NOT NULL DEFAULT 0," +
					"UNIQUE(" + INNER_ID + "," + GLOBAL_ID + ")" +
				")";

	private static final String CREATE_TASKS = 
			"CREATE TABLE " + TABLE_TASKS + " (" +
					INNER_ID + " INT PRIMARY KEY," +
					GLOBAL_ID + " INT(10)," +
					TABLE_ID + " INT(10) NOT NULL," +
					UPDATE_TIME + " INT(10) NOT NULL DEFAULT 0," +
					"FOREIGN KEY (" + TABLE_ID + ") REFERENCES tables(" + INNER_ID + ")," +
					"UNIQUE(" + INNER_ID + "," + GLOBAL_ID + ")" +
				")";
	
	private static final String CREATE_TABLES_CHANGES = 
			"CREATE TABLE " + TABLE_TABLE_CHANGES + " (" +
					TABLE_ID + " INT(10)," +
					TIME + " INT(10) NOT NULL," +
					USER_ID + " INT(10) NOT NULL," +
					CHANGE_NAME + " VARCHAR(100)," +
					CHANGE_DESCRIPTION + " TEXT," +
					"FOREIGN KEY (" + TABLE_ID + ") REFERENCES tables(" + INNER_ID + ")," +
					"FOREIGN KEY (" + USER_ID + ") REFERENCES users(" + GLOBAL_ID + ")," +
					"UNIQUE(" + TABLE_ID + "," + TIME + ")" +
				")";

	private static final String CREATE_TASKS_CHANGES = 
			"CREATE TABLE " + TABLE_TASK_CHANGES + " (" +
					TABLE_ID + " INT(10) NOT NULL," +
					TASK_ID + " INT(10) NOT NULL," +
					TIME + " INT(10) NOT NULL," +
					USER_ID + " INT(10) NOT NULL," +
					CHANGE_NAME + " VARCHAR(100)," +
					CHANGE_DESCRIPTION + " TEXT," +
					CHANGE_TASK_START_DATE + " DATE," +
					CHANGE_TASK_END_DATE + " DATE, " +
					CHANGE_TASK_START_TIME + " TIME, " +
					CHANGE_TASK_END_TIME + " TIME, " +
					"FOREIGN KEY (" + TABLE_ID + ") REFERENCES tables(" + INNER_ID + ")," +
					"FOREIGN KEY (" + TASK_ID + ") REFERENCES tasks(" + INNER_ID + ")," +
					"UNIQUE(" + TABLE_ID + ", " + TASK_ID + ")" +
				")";

	private static final String CREATE_COMMENTS = 
			"CREATE TABLE comments (" +
					USER_ID + " INT(10) NOT NULL," +
					TABLE_ID + " INT(10) NOT NULL," +
					TASK_ID + " INT(10) NOT NULL," +
					COMMENTS_TEXT + " TEXT NOT NULL," +
					TIME + " INT(10) NOT NULL," +
					"FOREIGN KEY (" + USER_ID + ") REFERENCES users(id)," +
					"FOREIGN KEY (" + TABLE_ID + ") REFERENCES tables(" + INNER_ID + ")," +
					"FOREIGN KEY (" + TASK_ID + ") REFERENCES tasks(" + INNER_ID + ")" +
				")";

	private static final String CREATE_READERS = 
			"CREATE TABLE " + TABLE_READERS + " (" +
					USER_ID + " INT(10) NOT NULL," +
					TABLE_ID + " INT(10) NOT NULL," +
					READERS_PERMISSION + " TINYINT(1) NOT NULL," +
					"FOREIGN KEY (" + USER_ID + ") REFERENCES users(" + GLOBAL_ID + ")," +
					"FOREIGN KEY (" + TABLE_ID + ") REFERENCES tables(" + INNER_ID + ")," +
					"UNIQUE(" + USER_ID + ", " + TABLE_ID + ")" +
				")";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_USERS);
		db.execSQL(CREATE_TABLES);
		db.execSQL(CREATE_TASKS);
		db.execSQL(CREATE_TABLES_CHANGES);
		db.execSQL(CREATE_TASKS_CHANGES);
		db.execSQL(CREATE_COMMENTS);
		db.execSQL(CREATE_READERS);

		db.execSQL("INSERT INTO users(id, name) VALUES (0, \"\")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
	}
}
