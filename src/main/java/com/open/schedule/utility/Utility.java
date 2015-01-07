package com.open.schedule.utility;

public class Utility {
	public static Byte getByte(byte[] buffer, int offset) {
		return new Byte(buffer[offset]);
	}
	
	public static Short getShort(byte[] buffer, int offset) {
		int size = Short.SIZE / 8;
		Short result = new Short((short) 0);
		for (int i = 0; i < size; i++) 
			result = (short) ((result << 8) | buffer[offset + i]);
		return result;
	}

	public static Short getShort(char[] buffer, int offset) {
		int size = Short.SIZE / 8;
		Short result = new Short((short) 0);
		for (int i = 0; i < size; i++)
			result = (short) ((result << 8) | buffer[offset + i]);
		return result;
	}

	public static Integer getInt(byte[] buffer, int offset) {
		Integer result = 0;
		int size = Integer.SIZE / 8;
		for (int i = 0; i < size; i++) 
			result = (result << 8) | buffer[offset + i];
		return result;
	}

	public static Long getLong(byte[] buffer, int offset) {
		Long result = Integer.valueOf(0).longValue();
		int size = Long.SIZE / 8;
		for (int i = 0; i < size; i++) 
			result = (result << 8) | buffer[offset + i];
		return result;
	}

	public static short bytesToShort(byte[] buffer) {
		return (short)(buffer[0] | buffer[1] << 8);
	}
	
	public static long getUnixTime() {
		return System.currentTimeMillis() / 1000L;
	}
}