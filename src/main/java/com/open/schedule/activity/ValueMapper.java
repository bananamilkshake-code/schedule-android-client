package com.open.schedule.activity;

import android.widget.TextView;

public class ValueMapper {
	public final String intentExtra;
	public final boolean canBeEmpty;
	public final int viewId;

	public TextView view;
	public String oldValue;

	ValueMapper(String intentExtra, int viewId, boolean canBeEmpty) {
		this.intentExtra = intentExtra;
		this.viewId = viewId;
		this.canBeEmpty = canBeEmpty;
	}
}
