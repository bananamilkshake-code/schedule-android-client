package com.open.schedule.io;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ServerConnector {
	private static final Integer TIMEOUT_CONNECTION_RETRY = 1 * 60 * 1000;

	private EventLoopGroup workerGroup = new NioEventLoopGroup();

	private Bootstrap bootstrap = new Bootstrap();

	private Channel channel = null;

	public ServerConnector(final PacketDecoder packetDecoder, final Client client) {
		this.bootstrap.group(workerGroup);
		this.bootstrap.channel(NioSocketChannel.class);
		this.bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel channel) throws Exception {
				channel.pipeline().addLast(packetDecoder, client);
			}
		});
	}

	public void tryConnect(final String host, final int port) {
		new Thread() {
			@Override
			public void run() {
				if (!ServerConnector.this.isConnected()) {
					try {
						ChannelFuture future = bootstrap.connect(host, port).sync();
						ServerConnector.this.channel = future.channel();
						ServerConnector.this.channel.closeFuture().sync();
					} catch (InterruptedException e) {
						ServerConnector.this.channel = null;
					}
				}

				try {
					Thread.sleep(ServerConnector.TIMEOUT_CONNECTION_RETRY);
				} catch (InterruptedException exc) {}
			}
		}.start();
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
