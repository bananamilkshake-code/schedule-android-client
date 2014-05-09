package utility;

public class Utility {
	public static Byte getByte(char[] buffer, int offset) {
		return new Byte((byte) buffer[offset]);
	}
	
	public static Short getShort(char[] buffer, int offset) {
		int size = Short.SIZE / 8;
		Short result = new Short((short) 0);
		for (int i = 0; i < size; i++) 
			result = Integer.valueOf(result | Integer.valueOf(buffer[i] << (8 * i)).shortValue()).shortValue();
		return result;
	}

	public static Integer getInt(char[] buffer, int offset) {
		Integer result = 0;
		int size = Integer.SIZE / 8;
		for (int i = 0; i < size; i++) 
			result = (result << 8) | buffer[offset + i];
		return result;
	}

	public static Long getLong(char[] buffer, int offset) {
		Long result = Integer.valueOf(0).longValue();
		int size = Long.SIZE / 8;
		for (int i = 0; i < size; i++) 
			result = (result << 8) | buffer[offset + i];
		return result;
	}

	public static short bytesToShort(char[] buffer) {
		return (short)(buffer[0] | buffer[1] << 8);
	}
	
	public static long getUnixTime() {
		return System.currentTimeMillis() / 1000L;
	}
}
