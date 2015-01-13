package com.open.schedule.io;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.open.schedule.account.Account;
import com.open.schedule.account.tables.ChangeableData;
import com.open.schedule.account.tables.Table;
import com.open.schedule.account.tables.Task;
import com.open.schedule.activity.UiMessageHandler;
import com.open.schedule.activity.UiMessageType;
import com.open.schedule.io.packet.*;
import com.open.schedule.io.packet.client.CreateTablePacket;
import com.open.schedule.io.packet.client.CreateTaskPacket;
import com.open.schedule.io.packet.client.LoginPacket;
import com.open.schedule.io.packet.client.RegisterPacket;
import com.open.schedule.io.packet.server.LoggedPacket;
import com.open.schedule.io.packet.server.RegisteredPacket;
import com.open.schedule.io.packet.server.TablePacket;
import com.open.schedule.io.packet.server.TaskPacket;
import static com.open.schedule.activity.UiMessageType.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class Client extends ChannelDuplexHandler {
	private final static String LOG_TAG = "SCHEDULE_CLIENT";

	private final Account account;

	private ChannelHandlerContext context = null;
	private boolean logged = false;

	private final HashSet<Handler> messageHandlers[] = new HashSet[UiMessageType.values().length];

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("ddMMyyyy", Locale.US);
	private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HHmm", Locale.US);

	public Client(Account account) {
		this.account = account;

		for (int i = 0; i < this.messageHandlers.length; ++i) {
			this.messageHandlers[i] = new HashSet<>();
		}
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		this.context = ctx;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ServerPacket packet = (ServerPacket) msg;

		if (packet.getType().needLogged && !this.isLogged()) {
			throw new IllegalStateException("Received packets when not logged in");
		} else if (!packet.getType().needLogged && this.isLogged()) {
			throw new IllegalStateException("Received auth packets when already logged in");
		}

		switch (packet.getType()) {
			case REGISTERED:
				this.registered((RegisteredPacket) packet);
				break;
			case LOGGED:
				this.logged((LoggedPacket) packet);
				break;
			case TABLE:
				this.newTable((TablePacket) packet);
				break;
			case TASK:
				this.newTask((TaskPacket) packet);
				break;
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		Log.w(LOG_TAG, "Netty Exception", cause);

		ctx.close();
	}

	public void logout() {
		this.logged = false;
	}

	public boolean isLogged() {
		return logged;
	}

	public void send(ClientPacket packet) {
		short size = packet.getSize();

		int bufferCapacity = (Packet.PACKET_TYPE_LENGTH + Packet.PACKET_SIZE_LENGTH + size);

		ByteBuf data = this.context.alloc().buffer(bufferCapacity, bufferCapacity);

		data.writeByte(packet.getType().ordinal());
		data.writeShort(size * Byte.SIZE);
		data.writeBytes(packet.getBuffer(), 0, (int) size);

		this.context.writeAndFlush(data);
	}

	public void login(String username, String password, final UiMessageHandler activity) {
		this.addMessageListener(UI_MESSAGE_LOGGED, activity);

		this.send(new LoginPacket(username, password));
	}

	public void register(String email, String password, String name, final UiMessageHandler activity) {
		this.addMessageListener(UI_MESSAGE_REGISTERED, activity);

		this.send(new RegisterPacket(email, password));
	}

	public void sync(ChangeableData data) {
		if (!this.logged)
			return;

		int id = data.getId();
		ChangeableData.Change info = data.getData();

		if (data instanceof Table) {
			Table.TableChange tableInfo = (Table.TableChange) info;

			this.send(new CreateTablePacket(id, tableInfo.time, tableInfo.name, tableInfo.description));
		} else if (data instanceof Task) {
			Task.TaskChange taskInfo = (Task.TaskChange) info;

			this.send(new CreateTaskPacket(id, ((Task) data).getTableId(), taskInfo.time, taskInfo.name, taskInfo.description,
					DATE_FORMATTER.format(taskInfo.startDate), DATE_FORMATTER.format(taskInfo.endDate),
					TIME_FORMATTER.format(taskInfo.startTime), TIME_FORMATTER.format(taskInfo.endTime)));
		}
	}

	private void registered(RegisteredPacket packet) {
		switch (packet.status) {
			case SUCCESS:
				Log.d(LOG_TAG, "Successfully registered in");
				break;
			case FAILURE:
				Log.w(LOG_TAG, "Username has already being used");
				break;
		}

		this.notify(UI_MESSAGE_REGISTERED, packet.status);
	}

	private void logged(LoggedPacket packet) {
		switch (packet.status) {
			case SUCCESS:
				Log.d(LOG_TAG, "Successfully logged in");

				logged = true;

				this.account.setId(packet.id);
				break;
			case FAILURE:
				Log.w(LOG_TAG, "Wrong username or password");
				break;
		}

		this.notify(UI_MESSAGE_LOGGED, packet.status);
	}

	private void newTable(TablePacket packet) {
		this.account.createTable(packet.name, packet.description, packet.userId, false);
	}

	private void newTask(TaskPacket packet) {
		Date startDate = null;
		Date endDate = null;
		Date startTime = null;
		Date endTime = null;

		try {
			startDate = DATE_FORMATTER.parse(packet.startDate);
			endDate = DATE_FORMATTER.parse(packet.endDate);
			startTime = TIME_FORMATTER.parse(packet.startTime);
			endTime = TIME_FORMATTER.parse(packet.endTime);
		} catch (ParseException e) {
			Log.w(LOG_TAG, "NewTask parsing exception", e);
		}

		this.account.createTask(packet.tableId, packet.name, packet.description, packet.userId, startDate, endDate, startTime, endTime, packet.period, false);
	}

	private void addMessageListener(final UiMessageType messageType, final UiMessageHandler activity) {
		HashSet<Handler> handlers = this.messageHandlers[messageType.ordinal()];

		synchronized (handlers) {
			handlers.add(new Handler(Looper.getMainLooper()) {
				@Override
				public void handleMessage(Message inputMessage) {
					activity.handleMessage(inputMessage);
				}
			});
		}
	}

	private void notify(UiMessageType messageType, Object data) {
		HashSet<Handler> handlers = this.messageHandlers[messageType.ordinal()];
		
		synchronized (handlers) {
			for (Handler handler : handlers) {
				Message message = handler.obtainMessage(messageType.ordinal(), data);
				message.sendToTarget();
			}

			handlers.clear();
		}
	}
}