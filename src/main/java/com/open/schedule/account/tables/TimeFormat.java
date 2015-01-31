package com.open.schedule.account.tables;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimeFormat {
	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
	public static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm", Locale.US);
}
