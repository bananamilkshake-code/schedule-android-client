package com.open.schedule.io;

import android.util.Log;

import com.open.schedule.io.packet.ServerPacket;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

public class PacketDecoder extends ReplayingDecoder<PacketDecoderState> {
	private ServerPacket.Type type;
	private short length;

	public PacketDecoder() {
		super(PacketDecoderState.STATE_READ_TYPE);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		switch (this.state())
		{
			case STATE_READ_TYPE:
				this.type = ServerPacket.Type.values()[in.readByte()];
				this.checkpoint(PacketDecoderState.STATE_READ_LENGTH);
			case STATE_READ_LENGTH:
				this.length = in.readShort();
				this.checkpoint(PacketDecoderState.STATE_READ_CONTENT);
			case STATE_READ_CONTENT:
				ServerPacket packet = ServerPacket.get(this.type);

				byte[] data = new byte[this.length];
				in.readBytes(data, 0, this.length);

				packet.init(data);

				out.add(packet);

				Log.d("PacketDecoder", "Packet with type " + this.type.toString() + " received");

				this.checkpoint(PacketDecoderState.STATE_READ_TYPE);
		}
	}
}

