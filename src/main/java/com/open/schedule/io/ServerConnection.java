package com.open.schedule.io;

import android.util.Log;

import com.open.schedule.app.ScheduleApplication;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ServerConnection {
	private final static String LOG_TAG = ServerConnection.class.getName();

	final private EventLoopGroup workerGroup = new NioEventLoopGroup();
	final private Bootstrap bootstrap = new Bootstrap();

	private String host;
	private int port;

	private Channel channel = null;

	private final ChannelFutureListener channelCloseListener;

	public ServerConnection(final ScheduleApplication application, final Client client, final String host, final int port) {
		this.host = host;
		this.port = port;

		this.bootstrap
			.group(workerGroup)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel channel) throws Exception {
					channel.pipeline().addLast(new PacketDecoder(), client);
				}
			});

		this.channelCloseListener = new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				client.logout();

				application.new ConnectionAsyncTask().execute();
			}
		};
	}

	public void tryConnect() {
		try {
			ChannelFuture future = bootstrap.connect(this.host, this.port).sync();
			Channel channel = future.channel();
			this.channel = channel;

			channel.closeFuture().addListener(this.channelCloseListener);
		} catch (Exception e) {
			Log.w("ServerConnector", "Exception on connection", e);

			this.channel = null;
		}
	}

	public void disconnect() {
		workerGroup.shutdownGracefully();

		this.channel.close();
		this.channel = null;
	}

	public boolean isConnected() {
		return (this.channel != null && this.channel.isActive());
	}
}
