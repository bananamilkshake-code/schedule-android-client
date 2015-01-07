package com.open.schedule.io;

import android.util.Log;

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
	static final private int RETRY_TIMEOUT = (1 * 60 * 1000);

	final private EventLoopGroup workerGroup = new NioEventLoopGroup();
	final private Bootstrap bootstrap = new Bootstrap();

	private String host;
	private int port;

	private Channel channel = null;

	public ServerConnection(final PacketDecoder packetDecoder, final Client client, final String host, final int port) {
		this.host = host;
		this.port = port;

		this.bootstrap
			.group(workerGroup)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel channel) throws Exception {
					channel.pipeline().addLast(packetDecoder, client);
				}
			});
	}

	public void tryConnect() {
		try {
			ChannelFuture future = bootstrap.connect(host, port).sync();
			Channel channel = future.channel();

			this.channel = channel;

			channel.closeFuture().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					Log.w("ServerConnector", "Connection is closed");
					ServerConnection.this.retryAfterTimeout();
				}
			});
		} catch (Exception e) {
			Log.w("ServerConnector", "Exception on connection", e);
		}
	}

	private void retryAfterTimeout() {
		try {
			Thread.sleep(RETRY_TIMEOUT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			this.tryConnect();
		}
	}

	protected void finalize() throws Exception {
		if (this.isConnected())
			disconnect();
	}

	public void disconnect() {
		workerGroup.shutdownGracefully();

		this.channel = null;
	}

	public boolean isConnected() {
		return (this.channel != null && this.channel.isActive());
	}
}
