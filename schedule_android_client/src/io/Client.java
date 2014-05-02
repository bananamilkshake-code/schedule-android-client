package io;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import storage.database.Database;
import storage.tables.Table;
import storage.tables.Table.Permission;
import storage.tables.Task;
import utility.Utility;
import android.util.Log;
import config.Config;
import events.objects.Event;
import io.packet.ServerPacket;
import io.packet.server.RegisterPacket;
import io.packet.server.LoginPacket;

public class Client extends TCPClient {	
	private static Client INSTANCE = null;

	private boolean logged = false;
	private Integer id = 0;

	private HashMap<Integer, Table> tables = new HashMap<Integer, Table>();

	private Database database = null;

	protected Client(Database database) {
		super(Config.host, Config.port);
		this.database = database;

		database.loadTables(tables);
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

	public void recv(ServerPacket clientPacket) {
		switch (clientPacket.getType()) {
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

		switch (clientPacket.getType())
		{
		case REGISTER:
			RegisterPacket register = (RegisterPacket)clientPacket;	
			switch (register.status) {
			case SUCCESS:
				Log.d("Recv", "Successfully registered in");
				return;	
			case FAILURE:
				Log.w("Recv", "Username has already being used");
				return;
			}
			break;
		case LOGIN:
			LoginPacket login = (LoginPacket)clientPacket;
			switch (login.status) {
			case SUCCESS:
				Log.d("Recv", "Successfully logged in");
				logged = true;
				break;
			case FAILURE:
				Log.w("Recv", "Wrong username or password");
				break;
			}
			new Event(this, Event.Type.LOGIN, (Object)login.status);
			break;
		default:
			break;
		}
	}
	
	public boolean isLogged() {
		return logged;
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

	public void createTable(Integer userId, Boolean local, String name, String description) {
		Table table = new Table(this.id, Utility.getUnixTime(), name, description);
		Integer newTableId = database.createTable(userId, table);
		tables.put(newTableId, table);
		changePermision(local, newTableId, getId(), Permission.WRITE);
		Log.d("Client", "New table created " + newTableId);

		if (local)
		try {
			send(new io.packet.client.CreateTablePacket(Utility.getUnixTime(), name, description));
		} catch (IOException e) {
			Log.w("Client", "New table creation error", e);
		}
	}

	SimpleDateFormat dateFormatter = new SimpleDateFormat("ddMMyyyy");
	SimpleDateFormat timeFormatter = new SimpleDateFormat("HHmm");

	public void createTask(Integer userId, Boolean local, Long time, Integer tableId, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime) {
		Table table = tables.get(tableId);
		Task task = new Task(userId, time, name, description, startDate, endDate, startTime, endTime);
		Integer taskId = database.createTask(userId, tableId, task);
		table.addTask(taskId, task);
		Log.d("Client", "New task " + taskId + " for table " + tableId + " created");

		if (local) {
			String startDateVal = dateFormatter.format(startDate);
			String endDateVal = dateFormatter.format(endDate);
			String startTimeVal = timeFormatter.format(startTime);
			String endTimeVal = timeFormatter.format(endTime);

			try {
				send(new io.packet.client.CreateTaskPacket(tableId, time, name, description, startDateVal, endDateVal, startTimeVal, endTimeVal));
			} catch (IOException e) {
				Log.w("Client", "New task creation error", e);
			}
		}
	}

	public void changeTable(Integer userId, Boolean local, Integer tableId, String name, String description) {
		Table table = tables.get(tableId);
		table.change(table.new TableInfo(userId, Utility.getUnixTime(), name, description));
		Log.d("Client", "Table " + tableId + " changed");

		if (local) {
			try {
				send(new io.packet.client.ChangeTablePacket(tableId, Utility.getUnixTime(), name, description));
			} catch (IOException e) {
				Log.w("Client", "Table " + tableId + " change error", e);
			}
		}
	}
	
	public void changeTask(Integer userId, Boolean local, Long time, Integer tableId, Integer taskId, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime) {
		Table table = tables.get(tableId);
		Task task = table.getTask(taskId);
		task.change(task.new TaskChange(userId, time, name, description, startDate, endDate, startTime, endTime));
		
		if (local) {
			String startDateVal = dateFormatter.format(startDate);
			String endDateVal = dateFormatter.format(endDate);
			String startTimeVal = timeFormatter.format(startTime);
			String endTimeVal = timeFormatter.format(endTime);

			try {
				send(new io.packet.client.ChangeTaskPacket(taskId, tableId, time, name, description, startDateVal, endDateVal, startTimeVal, endTimeVal));
			} catch (IOException e) {
				Log.w("Client", "New task creation error", e);
			}
		}
	}

	public void createComment(Integer userId, Boolean local, Integer tableId, Integer taskId, String comment, Long time) {
		tables.get(tableId).getTask(taskId).addComment(userId, time, comment);
		Log.d("Client", "New comment added for (" + tableId + "," + taskId + "): " + comment);

		if (local) {
			try {
				send(new io.packet.client.CreateComment(tableId, taskId, time, comment));
			} catch (IOException e) {
				Log.w("Client", "New comment creation error", e);
			}
		}
	}

	public void changePermision(Boolean local, Integer tableId, Integer userId, Permission permission) {
		tables.get(tableId).setPermission(userId, permission);
		Log.d("Client", "Permission for user " + userId + " changed to " + permission.ordinal());

		if (local) {
			try {
				send(new io.packet.client.PermissionPacket(userId, tableId, (byte)(permission.ordinal())));
			} catch (IOException e) {
				Log.w("Client", "Changin permission error", e);
			}
		}
	}

	public final HashMap<Integer, Table> getTables() {
		return tables;
	}

	@Override
	protected void clear() {
		logged = false;
	}
}
