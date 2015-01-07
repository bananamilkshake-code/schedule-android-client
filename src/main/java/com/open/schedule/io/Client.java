package com.open.schedule.io;

import java.io.IOException;
import java.net.SocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SortedMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.util.Log;

import com.open.schedule.io.packet.Packet;
import com.open.schedule.storage.database.Database;
import com.open.schedule.storage.tables.ChangeableData.Change;
import com.open.schedule.storage.tables.Table;
import com.open.schedule.storage.tables.Table.Permission;
import com.open.schedule.storage.tables.Table.TableInfo;
import com.open.schedule.storage.tables.Task;
import com.open.schedule.storage.tables.Task.TaskChange;
import com.open.schedule.storage.tables.Users;
import com.open.schedule.events.objects.Event;
import com.open.schedule.io.packet.ClientPacket;
import com.open.schedule.io.packet.ServerPacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class Client extends ChannelDuplexHandler {
	private ChannelHandlerContext context;

	private boolean logged = false;

	private Integer id = 0;
	private Long logoutTime = (long) 0;

	public final Tables tables  = new Tables();
	public final Users users = new Users();

	private Database database = null;

	public Client(final Database database) {
		this.database = database;

		database.loadTables(this.tables);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ServerPacket packet = (ServerPacket) msg;

		switch (packet.getType()) {
			case REGISTER:
			case LOGIN:
				if (isLogged()) {
					Log.e("Client.recv", "Received auth packets when already logged in");
					System.exit(1);
				}
				break;
			default:
				if (!isLogged()) {
					Log.e("Client.recv", "Received packets when not logged in");
					System.exit(1);
				}
				break;
		}

		switch (packet.getType())
		{
			case REGISTER:
				this.register((com.open.schedule.io.packet.server.RegisterPacket) packet);
				break;
			case LOGIN:
				this.login((com.open.schedule.io.packet.server.LoginPacket) packet);
				break;
			case CHANGE_TABLE:
				this.changeTable((com.open.schedule.io.packet.server.ChangeTablePacket) packet);
				break;
			case CHANGE_TASK:
				this.changeTask((com.open.schedule.io.packet.server.ChangeTaskPacket) packet);
				break;
			case COMMENTARY:
				this.createCommentary((com.open.schedule.io.packet.server.CommentaryPacket) packet);
				break;
			case GLOBAL_TABLE_ID:
				this.updateGlobalTableId((com.open.schedule.io.packet.server.GlobalTableIdPacket) packet);
				break;
			case GLOBAL_TASK_ID:
				this.updateGlobalTaskId((com.open.schedule.io.packet.server.GlobalTaskIdPacket) packet);
				break;
			case PERMISSION:
				this.changePermission((com.open.schedule.io.packet.server.PermissionPacket) packet);
				break;
			case USER:
				this.addUser((com.open.schedule.io.packet.server.UserPacket) packet);
				break;
			default:
				break;
		}
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise future) throws Exception {
		super.connect(ctx, remoteAddress, localAddress, future);

		this.context = ctx;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		Log.w("Client", "Netty Exception", cause);
		ctx.close();
	}

	public Integer getId() {
		return id;
	}
	
	public boolean isLogged() {
		return logged;
	}

	public final SortedMap<Integer, Table> getTables() {
		return tables.getTables();
	}

	public final Users getUsers() {
		return this.users;
	}

	public void send(ClientPacket packet) {
		try {
			this.write(packet);
		} catch (IOException e) {
			Log.e("Client", "Error on message sending", e);
		}
	}

	public void write(ClientPacket packet) throws IOException {
		short size = packet.getSize();

		int bufferCapacity = (Packet.PACKET_TYPE_LENGTH + Packet.PACKET_SIZE_LENGTH + size);

		ByteBuf data = context.alloc().buffer(bufferCapacity, bufferCapacity);

		data.writeByte(packet.getType().ordinal());
		data.writeShort(size * Byte.SIZE);
		data.writeBytes(packet.getBuffer(), 0, (int) size);

		context.writeAndFlush(data);
	}

	public void loadAuthParams() {
		String username = "l@m.c";
		String password = "1111";
		this.login(username, password);
	}
	
	private void do_login(Integer id) {
		logged = true;
		this.id = id;
		database.updateUserId(id);
		
		sync();
	}
	
	private void sync() {
		syncTables();
		syncTasks();
		syncTableChanges();
		syncTaskChanges();
		syncComments();
	}
	
	private void syncTables() {
		ArrayList<Integer> tableList = tables.getNewTables();
		for (Integer tableId : tableList)
			syncTable(tableId);
	}
	
	private void syncTasks() {
		SortedMap<Integer, ArrayList<Integer>> tasks = tables.getNewTasks();
		Iterator<Entry<Integer, ArrayList<Integer>>> taskIter = tasks.entrySet().iterator();
		while (taskIter.hasNext()) {
			Entry<Integer, ArrayList<Integer>> task = taskIter.next();
			Integer tableId = task.getKey();
			for (Integer taskId : task.getValue())
				syncTask(tableId, taskId);
		}
	}
	
	private void syncTableChanges() {
		TreeMap<Integer, ArrayList<Long>> unsyncedTablesChanges = tables.getNewTableChanges(this.logoutTime, this.id);
		Iterator<Entry<Integer, ArrayList<Long>>> tableIter = unsyncedTablesChanges.entrySet().iterator();
		while (tableIter.hasNext()) {
			Entry<Integer, ArrayList<Long>> table = tableIter.next();
			Integer tableId = table.getKey();
			ArrayList<Long> times = table.getValue();
			for (Long time : times) {
				syncChangeTable(tableId, time);
				tables.updateTable(tableId, time);
				database.updateTable(tableId, time);
			}
		}
	}
	
	private void syncTaskChanges() {
		SortedMap<Integer, SortedMap<Integer, ArrayList<Long>>> unsyncedTaskChanges = tables.getNewTaskChanges(this.logoutTime, this.id);
		Iterator<Entry<Integer, SortedMap<Integer, ArrayList<Long>>>> tableIter = unsyncedTaskChanges.entrySet().iterator();
		while (tableIter.hasNext()) {
			Entry<Integer, SortedMap<Integer, ArrayList<Long>>> table = tableIter.next();
			Integer tableId = table.getKey();
			
			Iterator<Entry<Integer, ArrayList<Long>>> taskIter = table.getValue().entrySet().iterator();
			while (taskIter.hasNext()) {
				Entry<Integer, ArrayList<Long>> task = taskIter.next();
				Integer taskId = task.getKey();
				
				ArrayList<Long> changesTimes = task.getValue();
				for (Long time : changesTimes) {
					syncChangeTask(tableId, taskId, time);
					tables.updateTask(tableId, taskId, time);
					database.updateTask(tableId, taskId, time);
				}
			}
		}
	}
	
	private void syncComments() {
		SortedMap<Integer, SortedMap<Integer, ArrayList<Long>>> comments = tables.getNewComments(this.logoutTime, this.id);
		Iterator<Entry<Integer, SortedMap<Integer, ArrayList<Long>>>> tableIter = comments.entrySet().iterator();
		while (tableIter.hasNext()) {
			Entry<Integer, SortedMap<Integer, ArrayList<Long>>> table = tableIter.next();
			Integer tableId = table.getKey();

			Iterator<Entry<Integer, ArrayList<Long>>> taskIter = table.getValue().entrySet().iterator();
			while (taskIter.hasNext()) {
				Entry<Integer, ArrayList<Long>> task = taskIter.next();
				Integer taskId = task.getKey();
				ArrayList<Long> times = task.getValue();
				for (Long time : times)
					syncComment(tableId, taskId, time);
			}
		}
	}
	
	public void login(String username, String password) {
		try {
			send(new com.open.schedule.io.packet.client.LoginPacket(username, password));
		} catch (IOException e) {
			Log.w("Client", "Authorization error", e);
		}
	}

	public void register(String email, String password, String name) {
		try {
			send(new com.open.schedule.io.packet.client.RegisterPacket(email, password));
		} catch (IOException e) {
			Log.w("Client", "Registration error", e);
		}
	}

	public Integer createTable(Boolean local, Integer userId, Long time, String name, String description) {
		Integer tableId = database.createTable(userId, time, name, description);
		Table table = new Table(tableId, this.id, time, name, description);
		tables.createTable(tableId, table);
		changePermision(false, tableId, getId(), Permission.WRITE);
		Log.d("Client", "New table created " + tableId);

		if (local)
			syncTable(tableId);
		return tableId;
	}

	SimpleDateFormat dateFormatter = new SimpleDateFormat("ddMMyyyy", Locale.US);
	SimpleDateFormat timeFormatter = new SimpleDateFormat("HHmm", Locale.US);

	public Integer createTask(Boolean local, Integer tableId, Long time, Integer userId, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime, Integer period) {
		Integer taskId = database.createTask(userId, tableId, time, name, description, startDate, endDate, startTime, endTime, period);
		Task task = new Task(taskId, userId, time, name, description, startDate, endDate, startTime, endTime, period);
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
			syncComment(tableId, taskId, time);
	}

	public void changeTable(Boolean local, Integer tableId, Long time, Integer userId, String name, String description) {
		if (!local) {
			if (tables.findGlobalTable(tableId) == null)
				this.createTable(false, userId, time, name, description);
			Integer tableGlobalId = Integer.valueOf(tableId);
			tables.changeTableId(tableId);
			tables.updateTableGlobalId(tableId, tableGlobalId);
		}

		tables.changeTable(tableId, userId, time, name, description);
		database.changeTable(this.id, tableId, time, name, description);
		Log.d("Client", "Table " + tableId + " changed");

		if (local) 
			syncChangeTable(tableId, time);
	}

	public void changeTask(Boolean local, Integer tableId, Integer taskId, Long time, Integer userId, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime, Integer period) {
		if (!local) {
			Integer tableGlobalId = Integer.valueOf(tableId);
			tables.changeTableId(tableId);
			Integer taskGlobalId = Integer.valueOf(taskId);
			if (tables.findGlobalTask(tableId, taskId) == null)
				taskId = this.createTask(local, tableId, time, userId, name, description, startDate, endDate, startTime, endTime, period);
			tables.updateTaskGlobalId(tableGlobalId, taskGlobalId, taskId);
		}

		tables.changeTask(tableId, taskId, userId, time, name, description, startDate, endDate, startTime, endTime, period);
		database.changeTask(userId, tableId, taskId, time, name, description, startDate, endDate, startTime, endTime, period);
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
	
	private void register(com.open.schedule.io.packet.server.RegisterPacket packet) {
		switch (packet.status) {
		case SUCCESS:
			Log.d("Recv", "Successfully registered in");
			break;	
		case FAILURE:
			Log.w("Recv", "Username has already being used");
			break;
		}
		new Event(this, Event.Type.REGISTER, (Object) packet.status);
	}
	
	private void login(com.open.schedule.io.packet.server.LoginPacket packet) {
		switch (packet.status) {
		case SUCCESS:
			Log.d("Recv", "Successfully logged in");
			do_login(packet.id);
			break;
		case FAILURE:
			Log.w("Recv", "Wrong username or password");
			break;
		}
		new Event(this, Event.Type.LOGIN, (Object) packet.status);
	}

	private void changeTable(com.open.schedule.io.packet.server.ChangeTablePacket packet) {
		this.changeTable(false, packet.tableGlobalId, packet.time, packet.userId, packet.name, packet.description);
	}
	
	private void changeTask(com.open.schedule.io.packet.server.ChangeTaskPacket packet) {
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
					startDate, endDate, startTime, endTime, packet.period);
	}

	private void createCommentary(com.open.schedule.io.packet.server.CommentaryPacket packet) {
		this.createComment(false, packet.tableGlobalId, packet.taskGlobalId, packet.time, packet.userId, packet.comment);
	}

	private void updateGlobalTableId(com.open.schedule.io.packet.server.GlobalTableIdPacket packet) {
		tables.updateTableGlobalId(packet.tableId, packet.tableGlobalId);
		database.updateTableGlobalId(packet.tableId, packet.tableGlobalId);
		syncTasks();
		syncTableChanges();
	}
	
	private void updateGlobalTaskId(com.open.schedule.io.packet.server.GlobalTaskIdPacket packet) {
		tables.updateTaskGlobalId(packet.tableGlobalId, packet.taskGlobalId, packet.taskId);
		database.updateTaskGlobalId(packet.tableGlobalId, packet.taskGlobalId, packet.taskId);
		syncComments();
		syncTaskChanges();
	}

	private void changePermission(com.open.schedule.io.packet.server.PermissionPacket packet) {
		this.changePermision(false, packet.tableGlobalId, packet.userId, Table.Permission.values()[packet.permission]);
	}
	
	private void addUser(com.open.schedule.io.packet.server.UserPacket packet) {
		database.addUser(packet.userId, packet.name);
	}
	
	private void syncTable(Integer tableId) {
		Entry<Long, Change> entry = tables.getTables().get(tableId).getInitial();
		Long time = entry.getKey();
		TableInfo tableInfo = (TableInfo) entry.getValue();
		String name = tableInfo.name;
		String description = tableInfo.description;
		try {
			send(new com.open.schedule.io.packet.client.CreateTablePacket(tableId, time, name, description));
		} catch (IOException e) {
			Log.w("Client", "New table creation error", e);
		}
	}
	
	private void syncTask(Integer tableId, Integer taskId) {
		Integer tableGlobalId = tables.findInnerTable(tableId);
		if (tableGlobalId == null) {
			return;
		}

		Entry<Long, Change> entry = tables.getTables().get(tableId).getTask(taskId).getInitial();
		Long time = entry.getKey();
		TaskChange task = (TaskChange) entry.getValue();

		String startDateVal = dateFormatter.format(task.startDate);
		String endDateVal = dateFormatter.format(task.endDate);
		String startTimeVal = timeFormatter.format(task.startTime);
		String endTimeVal = timeFormatter.format(task.endTime);

		try {
			send(new com.open.schedule.io.packet.client.CreateTaskPacket(taskId, tableGlobalId, time, task.name, task.description, startDateVal, endDateVal, startTimeVal, endTimeVal));
		} catch (IOException e) {
			Log.w("Client", "New task creation error", e);
		}
	}

	private void syncComment(Integer tableId, Integer taskId, Long time) {
		String comment = tables.getTables().get(tableId).getTask(taskId).getComments().get(time).text;
		Integer tableGlobalId = tables.findInnerTable(tableId);
		Integer taskGlobalId = tables.findInnerTask(tableId, taskId);
		if (tableGlobalId == null || taskGlobalId == null) {
			return;
		}

		try {
			send(new com.open.schedule.io.packet.client.CreateComment(tableGlobalId, taskGlobalId, time, comment));
		} catch (IOException e) {
			Log.w("Client", "New comment creation error", e);
		}
	}

	private void syncChangeTable(Integer tableId, Long time) {
		Integer tableGlobalId = tables.findInnerTable(tableId);
		if (tableGlobalId == null) {
			return;
		}
		
		TableInfo tableInfo = (TableInfo) tables.getTables().get(tableId).getChange(time);
		String name = tableInfo.name;
		String description = tableInfo.description;

		try {
			send(new com.open.schedule.io.packet.client.ChangeTablePacket(tableGlobalId, time, name, description));
		} catch (IOException e) {
			Log.w("Client", "Table " + tableId + " change error", e);
		}
	}

	private void syncChangeTask(Integer tableId, Integer taskId, Long time) {
		Integer tableGlobalId = tables.findInnerTable(tableId);
		Integer taskGlobalId = tables.findInnerTask(tableId, taskId);
		if (tableGlobalId == null || taskGlobalId == null) {
			return;
		}
		
		TaskChange task = (TaskChange) tables.getTables().get(tableId).getTask(taskId).getChange(time);
		String startDateVal = dateFormatter.format(task.startDate);
		String endDateVal = dateFormatter.format(task.endDate);
		String startTimeVal = timeFormatter.format(task.startTime);
		String endTimeVal = timeFormatter.format(task.endTime);

		try {
			send(new com.open.schedule.io.packet.client.ChangeTaskPacket(taskGlobalId, tableGlobalId, time, task.name, task.description, startDateVal, endDateVal, startTimeVal, endTimeVal));
		} catch (IOException e) {
			Log.w("Client", "New task creation error", e);
		}
	}
	
	private void syncPermission(Integer userId, Integer tableId, Permission permission) {
		Integer tableGlobalId = tables.findInnerTable(tableId);
		if (tableGlobalId == null) {
			return;
		}

		try {
			send(new com.open.schedule.io.packet.client.PermissionPacket(tableGlobalId, userId, (byte)(permission.ordinal())));
		} catch (IOException e) {
			Log.w("Client", "Changing permission error", e);
		}
	}

	public String getUserName(Integer creatorId) {
		return creatorId.toString();
	}

	public void updateLogoutTime(long logoutTime) {
		this.database.updateLogoutTime(logoutTime);
	}
}
