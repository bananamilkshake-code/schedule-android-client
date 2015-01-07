package com.open.schedule.io;

public enum PacketDecoderState {
	STATE_READ_TYPE,
	STATE_READ_LENGTH,
	STATE_READ_CONTENT
};