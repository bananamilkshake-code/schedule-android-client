package utility;


public class Utility {
	public static Short getShort(char[] buffer) {
		int size = Short.SIZE / 8;
		Short result = new Short((short) 0);
		for (int i = 0; i < size; i++) {
			result = new Integer(result | new Integer(buffer[i] << (8 * i)).shortValue()).shortValue();
		}
		return result;
	}
	
	public static Integer getInt(char[] buffer, int offset) {
		Integer result = new Integer((int) 0);
		int size = Integer.SIZE / 8;
		for (int i = 0; i < size; i++) 
			result = (result << 8) | new Integer(buffer[offset + i]);
		return result;
	}
	
	public static short bytesToShort(char[] buffer) {
		return (short)(buffer[0] | buffer[1] << 8);
	}
	
	public static long getUnixTime() {
		return System.currentTimeMillis() / 1000L;
	}
}
