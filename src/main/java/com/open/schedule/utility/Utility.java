package com.open.schedule.utility;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {
	private static final String LOG_TAG = Utility.class.getName();

	public static long getUnixTime() {
		return System.currentTimeMillis() / 1000L;
	}

	public static String parseToString(Date date, SimpleDateFormat format) {
		if (date == null) {
			return null;
		}

		String dateString = null;
		try {
			dateString = format.format(date);
		} catch (Exception exception) {
			Log.w(LOG_TAG, "Exception on parsing date " + date.toString() + " to string", exception);
		} finally {
			return dateString;
		}
	}

	public static Date parseToDate(String date, SimpleDateFormat format) {
		if (date == null) {
			return null;
		}

		Date dateValue = null;
		try {
			dateValue = format.parse(date);
		} catch (Exception exception) {
			Log.w(LOG_TAG, "Exception on parsing date " + date.toString() + " to string", exception);
		} finally {
			return dateValue;
		}
	}
}
