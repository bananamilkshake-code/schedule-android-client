package io;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import android.util.Log;
import storage.database.Database;
import storage.tables.ChangableData.Change;
import storage.tables.Table;
import storage.tables.Table.Permission;
import storage.tables.Table.TableInfo;
import storage.tables.Task;
import storage.tables.Task.TaskChange;
import config.Config;
import events.objects.Event;
import io.packet.ServerPacket;

public class Client extends TCPClient {	
	private static Client INSTANCE = null;

	private  boolean logged = false;
	private Integer id = 0;

	private Tables tables  = new Tables();
	private Users users = new Users();

	private Database database = null;

	protected Client(Database database) {
		super(Config.host, Config.port);
		this.database = database;

		database.loadTables(this.tables.getTables());
	}

	public static void createInstance(Database database) {
		INSTANCE = new Client(database);
	}
	
	public static Client getInstance() {
		assert INSTANCE != null;
		return INSTANCE;
	}
	
	public Integer getId() {
		return id;
	}
	
	public boolean isLogged() {
		return logged;
	}

	public final HashMap<Integer, Table> getTables() {
		return tables.getTables();
	}

	@Override
	protected void clear() {
		logged = false;
	}

	@Override
	public void recv(ServerPacket packet) {
		switch (packet.getType()) {
		case REGISTER:
		case LOGIN:
			if (isLogged()) {
				Log.e("Recv", "Recieved auth packets when already logged in");
				System.exit(1);
			}
			break;
		default:
			if (!isLogged()) {
				Log.e("Recv", "Recieved packets when not logged in");
				System.exit(1);	
			}
			break;
		}

		switch (packet.getType())
		{
		case REGISTER:
			this.register((io.packet.server.RegisterPacket) packet);
			break;
		case LOGIN:
			this.login((io.packet.server.LoginPacket) packet);
			break;
		case CHANGE_TABLE:
			this.changeTable((io.packet.server.ChangeTablePacket) packet);
			break;
		case CHANGE_TASK:
			this.changeTask((io.packet.server.ChangeTaskPacket) packet);
			break;
		case COMMENTARY:
			this.createCommentary((io.packet.server.CommentaryPacket) packet);
			break;
		case GLOBAL_TABLE_ID:
			this.updateGlobalTableId((io.packet.server.GlobalTableIdPacket) packet);
			break;
		case GLOBAL_TASK_ID:
			this.updateGlobalTaskId((io.packet.server.GlobalTaskIdPacket) packet);
			break;
		case PERMISSION:
			this.changePermission((io.packet.server.PermissionPacket) packet);
			break;
		case USER:
			this.addUser((io.packet.server.UserPacket) packet);
			break;
		default:
			break;
		}
	}

	public void loadAuthParams() {
		String username = "l@m.c";
		String password = "qqqq";
		this.login(username, password);
	}
	
	public void login(String username, String password) {
		try {
			send(new io.packet.client.LoginPacket(username, password));
		} catch (IOException e) {
			Log.w("Client", "Authorization error", e);
		}
	}

	public Integer createTable(Boolean local, Integer userId, Long time, String name, String description) {
		Table table = new Table(this.id, time, name, description);
		Integer tableId = database.createTable(userId, table);
		tables.createTable(tableId, table);
		changePermision(false, tableId, getId(), Permission.WRITE);
		Log.d("Client", "New table created " + tableId);

		if (local)
			syncTable(tableId);
		
		return tableId;
	}

	SimpleDateFormat dateFormatter = new SimpleDateFormat("ddMMyyyy");
	SimpleDateFormat timeFormatter = new SimpleDateFormat("HHmm");

	public Integer createTask(Boolean local, Integer tableId, Long time, Integer userId, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime) {
		Task task = new Task(userId, time, name, description, startDate, endDate, startTime, endTime);
		Integer taskId = database.createTask(userId, tableId, task);
		tables.createTask(tableId, taskId, task);
		Log.d("Client", "New task " + taskId + " for table " + tableId + " created");

		if (local)
			syncTask(tableId, taskId);
		
		return taskId;
	}

	public void createComment(Boolean local, Integer tableId, Integer taskId, Long time, Integer userId, String comment) {
		tables.createComment(tableId, taskId, userId, time, comment);
		database.createComment(tableId, taskId, time, getId(), comment);
		Log.d("Client", "New comment added for (" + tableId + "," + taskId + "): " + comment);

		if (local) 
			syncComment(tableId, taskId, time, comment);
	}

	public void changeTable(Boolean local, Integer tableId, Long time, Integer userId, String name, String description) {
		if (!local) {
			if (tables.findGlobalTable(tableId) == null)
				this.createTable(false, userId, time, name, description);
			Integer tableGlobalId = new Integer(tableId);
			tables.changeTableId(tableId);
			tables.updateTableGlobalId(tableId, tableGlobalId);
		}

		tables.changeTable(tableId, userId, time, name, description);
		Log.d("Client", "Table " + tableId + " changed");

		if (local) 
			syncChangeTable(tableId, time);
	}

	public void changeTask(Boolean local, Integer tableId, Integer taskId, Long time, Integer userId, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime) {
		if (!local) {
			Integer tableGlobalId = new Integer(tableId);
			tables.changeTableId(tableId);
			Integer taskGlobalId = new Integer(taskId);
			if (tables.findGlobalTask(tableId, taskId) == null)
				taskId = this.createTask(local, tableId, time, userId, name, description, startDate, endDate, startTime, endTime);
			tables.updateTaskGlobalId(tableGlobalId, taskGlobalId, taskId);
		}

		tables.changeTask(tableId, taskId, userId, time, name, description, startDate, endDate, startTime, endTime);
		Log.d("Client", "Task " + taskId + " for table " + tableId + " changed");

		if (local) 
			syncChangeTask(tableId, taskId, time);
	}

	public void changePermision(Boolean local, Integer tableId, Integer userId, Permission permission) {
		tables.changePermission(tableId, userId, permission);
		database.setPermission(tableId, userId, permission);
		Log.d("Client", "Permission for user " + userId + " changed to " + permission.ordinal());

		if (local) 
			syncPermission(userId, tableId, permission);
	}
	
	private void register(io.packet.server.RegisterPacket packet) {
		switch (packet.status) {
		case SUCCESS:
			Log.d("Recv", "Successfully registered in");
			return;	
		case FAILURE:
			Log.w("Recv", "Username has already being used");
			return;
		}
	}
	
	private void login(io.packet.server.LoginPacket packet) {
		switch (packet.status) {
		case SUCCESS:
			Log.d("Recv", "Successfully logged in");
			logged = true;
			id = packet.id;
			database.updateUserId(id);
			break;
		case FAILURE:
			Log.w("Recv", "Wrong username or password");
			break;
		}
		new Event(this, Event.Type.LOGIN, (Object)packet.status);
	}

	private void changeTable(io.packet.server.ChangeTablePacket packet) {
		this.changeTable(false, packet.tableGlobalId, packet.time, packet.userId, packet.name, packet.description);
	}
	
	private void changeTask(io.packet.server.ChangeTaskPacket packet) {
		Date startDate = null;
		Date endDate = null;
		Date startTime = null;
		Date endTime = null;

		try {
			startDate = dateFormatter.parse(packet.startDate);
			endDate = dateFormatter.parse(packet.endDate);
			startTime = timeFormatter.parse(packet.startTime);
			endTime = timeFormatter.parse(packet.endTime);
		} catch (ParseException e) {
			Log.w("Client", "ChangeTaskPacket parsing", e);
		}
		
		this.changeTask(false, packet.tableGlobalId, packet.taskGlobalId, packet.time, packet.userId, packet.name, packet.description, 
					startDate, endDate, startTime, endTime);
	}

	private void createCommentary(io.packet.server.CommentaryPacket packet) {
		this.createComment(false, packet.tableGlobalId, packet.taskGlobalId, packet.time, packet.userId, packet.comment);
	}

	private void updateGlobalTableId(io.packet.server.GlobalTableIdPacket packet) {
		tables.updateTableGlobalId(packet.tableId, packet.tableGlobalId);
		database.updateTableGlobalId(packet.tableId, packet.tableGlobalId);
	}
	
	private void updateGlobalTaskId(io.packet.server.GlobalTaskIdPacket packet) {
		tables.updateTaskGlobalId(packet.tableGlobalId, packet.taskGlobalId, packet.taskId);
		database.updateTaskGlobalId(packet.tableGlobalId, packet.taskGlobalId, packet.taskId);
	}

	private void changePermission(io.packet.server.PermissionPacket packet) {
		this.changePermision(false, packet.tableGlobalId, packet.userId, Table.Permission.values()[packet.permission]);
	}
	
	private void addUser(io.packet.server.UserPacket packet) {
		users.add(packet.userId, packet.name);
		database.addUser(packet.userId, packet.name);
	}
	
	private void syncTable(Integer tableId) {
		Entry<Long, Change> entry = tables.getTables().get(tableId).getInitial();
		Long time = entry.getKey();
		TableInfo tableInfo = (TableInfo) entry.getValue();
		String name = tableInfo.name;
		String description = tableInfo.description;
		try {
			send(new io.packet.client.CreateTablePacket(time, name, description));
		} catch (IOException e) {
			Log.w("Client", "New table creation error", e);
		}
	}
	
	private void syncTask(Integer tableId, Integer taskId) {
		Integer tableGlobalId = tables.findInnerTable(tableId);
		if (tableGlobalId == null)
			return;
		
		Entry<Long, Change> entry = tables.getTables().get(tableId).getTask(taskId).getInitial();
		Long time = entry.getKey();
		TaskChange task = (TaskChange) entry.getValue();

		String startDateVal = dateFormatter.format(task.startDate);
		String endDateVal = dateFormatter.format(task.endDate);
		String startTimeVal = timeFormatter.format(task.startTime);
		String endTimeVal = timeFormatter.format(task.endTime);

		try {
			send(new io.packet.client.CreateTaskPacket(tableGlobalId, time, task.name, task.description, startDateVal, endDateVal, startTimeVal, endTimeVal));
		} catch (IOException e) {
			Log.w("Client", "New task creation error", e);
		}
	}
	
	private void syncComment(Integer tableId, Integer taskId, Long time, String comment) {
		Integer tableGlobalId = tables.findInnerTable(tableId);
		Integer taskGlobalId = tables.findInnerTask(tableId, taskId);
		if (tableGlobalId == null || taskGlobalId == null)
			return;

		try {
			send(new io.packet.client.CreateComment(tableGlobalId, taskGlobalId, time, comment));
		} catch (IOException e) {
			Log.w("Client", "New comment creation error", e);
		}
	}
	
	private void syncChangeTable(Integer tableId, Long time) {
		Integer tableGlobalId = tables.findInnerTable(tableId);
		if (tableGlobalId == null)
			return;
		
		TableInfo tableInfo = (TableInfo) tables.getTables().get(tableId).getChange(time);

		String name = tableInfo.name;
		String description = tableInfo.description;

		try {
			send(new io.packet.client.ChangeTablePacket(tableGlobalId, time, name, description));
		} catch (IOException e) {
			Log.w("Client", "Table " + tableId + " change error", e);
		}
	}

	private void syncChangeTask(Integer tableId, Integer taskId, Long time) {
		Integer tableGlobalId = tables.findInnerTable(tableId);
		Integer taskGlobalId = tables.findInnerTask(tableId, taskId);
		if (tableGlobalId == null || taskGlobalId == null)
			return;
		
		TaskChange task = (TaskChange) tables.getTables().get(tableId).getTask(taskId).getChange(time);

		String startDateVal = dateFormatter.format(task.startDate);
		String endDateVal = dateFormatter.format(task.endDate);
		String startTimeVal = timeFormatter.format(task.startTime);
		String endTimeVal = timeFormatter.format(task.endTime);

		try {
			send(new io.packet.client.ChangeTaskPacket(taskGlobalId, tableGlobalId, time, task.name, task.description, startDateVal, endDateVal, startTimeVal, endTimeVal));
		} catch (IOException e) {
			Log.w("Client", "New task creation error", e);
		}
	}
	
	private void syncPermission(Integer userId, Integer tableId, Permission permission) {
		Integer tableGlobalId = tables.findInnerTable(tableId);
		if (tableGlobalId == null)
			return;
	
		try {
			send(new io.packet.client.PermissionPacket(tableGlobalId, userId, (byte)(permission.ordinal())));
		} catch (IOException e) {
			Log.w("Client", "Changin permission error", e);
		}
	}
}
