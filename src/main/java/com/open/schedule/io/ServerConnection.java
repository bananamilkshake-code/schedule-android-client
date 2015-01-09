package com.open.schedule.io;

import android.util.Log;

import com.open.schedule.app.ScheduleApplication;

import java.net.Inet4Address;
import java.net.InetAddress;

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
	final private EventLoopGroup workerGroup = new NioEventLoopGroup();
	final private Bootstrap bootstrap = new Bootstrap();

	private String host;
	private int port;

	private Channel channel = null;

	private final ChannelFutureListener channelCloseListener;

	public ServerConnection(final ScheduleApplication application, final PacketDecoder packetDecoder, final Client client, final String host, final int port) {
		this.host = host;
		this.port = port;

		this.bootstrap
			.group(workerGroup)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel channel) throws Exception {
					if (channel.pipeline().first() == null)
						channel.pipeline().addLast(packetDecoder, client);
				}
			});

		this.channelCloseListener = new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
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

	protected void finalize() throws Exception {
		if (this.isConnected())
			disconnect();
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
