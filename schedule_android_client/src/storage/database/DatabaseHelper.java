package storage.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{
	public static final String DATABASE_NAME = "schedule";
	public static final String TABLE_USERS = "users";
	public static final String TABLE_TABLES = "tables";
	public static final String TABLE_TABLE_CHANGES = "table_changes";
	public static final String TABLE_TASKS = "tasks";
	public static final String TABLE_TASK_CHANGES = "task_changes";
	public static final String TABLE_COMMENTS = "comments";
	public static final String TABLE_READERS = "readers";
	
	private static final int DATABASE_VERSION = 1;

	private static final String CREATE_DB = 
		"CREATE TABLE " + TABLE_USERS + " (" +
				"id INT(10) AUTO_INCREMENT PRIMARY KEY," +
				"name VARCHAR(50) NOT NULL," +
				"UNIQUE(id) " +
			"); " +
			"CREATE TABLE " + TABLE_TABLES + " (" +
				"_id INT PRIMARY KEY," +
				"id INT(10)," +
				"last_update INT(10) NOT NULL DEFAULT 0," +
				"UNIQUE(local_id, id)" +
			");" +
			"CREATE TABLE " + TABLE_TASKS + " (" +
				"_id INT PRIMARY KEY," +
				"id INT(10)," +
				"table_id INT(10) NOT NULL," +
				"last_update INT(10) NOT NULL DEFAULT 0," +
				"FOREIGN KEY (table_id) REFERENCES tables(id)," +
				"UNIQUE(local_id, id)" +
			");" +
			"CREATE TABLE " + TABLE_TABLE_CHANGES + " (" +
				"local_table_id INT PRIMARY KEY," +
				"table_id INT(10)," +
				"time INT(10) NOT NULL," +
				"user_id INT(10) NOT NULL," +
				"name VARCHAR(100)," +
				"description TEXT," +
				"FOREIGN KEY (table_id) REFERENCES tables(id)," +
				"FOREIGN KEY (user_id) REFERENCES users(id)," +
				"UNIQUE(local_table_id, table_id)" +
			");" +
			"CREATE TABLE " + TABLE_TASK_CHANGES + " (" +
				"local_table_id INT(10) NOT NULL," +
				"local_task_id INT(10) NOT NULL," +
				"table_id INT(10)," +
				"task_id INT(10)," +
				"time INT(10) NOT NULL," +
				"user_id INT(10) NOT NULL," +
				"name VARCHAR(100)," +
				"description TEXT," +
				"start_date DATE," +
				"completion_date DATE, " +
				"end_time TIME," +
				"FOREIGN KEY (table_id) REFERENCES tables(id)," +
				"FOREIGN KEY (task_id) REFERENCES tasks(id)," +
				"UNIQUE(local_table_id, local_task_id)," +
				"UNIQUE(table_id, task_id)" +
			");" +
			"CREATE TABLE comments (" +
				"commentator_id INT(10) NOT NULL," +
				"table_id INT(10) NOT NULL," +
				"task_id INT(10) NOT NULL," +
				"commentary TEXT NOT NULL," +
				"time INT(10) NOT NULL," +
				"FOREIGN KEY (commentator_id) REFERENCES users(id)," +
				"FOREIGN KEY (table_id) REFERENCES tables(id)," +
				"FOREIGN KEY (task_id) REFERENCES tasks(id)" +
			");" +
			"CREATE TABLE " + TABLE_READERS + " (" +
				"reader_id INT(10) NOT NULL," +
				"table_id INT(10) NOT NULL," +
				"permission TINYINT(1) NOT NULL," +
				"FOREIGN KEY (reader_id) REFERENCES users(id)," +
				"FOREIGN KEY (table_id) REFERENCES tables(id)," +
				"UNIQUE(reader_id, table_id)" +
			");";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_DB);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
	}
}
