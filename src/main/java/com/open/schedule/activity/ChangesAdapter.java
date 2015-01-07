package com.open.schedule.activity;

import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.open.schedule.storage.tables.ChangeableData.Change;

import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.TreeMap;

public abstract class ChangesAdapter extends BaseAdapter {
	protected final SimpleDateFormat timeFormat = new SimpleDateFormat("dd MMMM, yyyy HH:mm");
	protected TreeMap<Long, Change> changes = new TreeMap<Long, Change>();
	protected LayoutInflater inflater;

	public ChangesAdapter(LayoutInflater inflater, TreeMap<Long, Change> changes) {
		this.inflater = inflater;
		this.changes = changes;
	}

	@Override
	public int getCount() {
		return changes.size();
	}

	@Override
	public Change getItem(int position) {
		return (Change) changes.entrySet().toArray(new Entry[this.changes.size()])[position].getValue();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}