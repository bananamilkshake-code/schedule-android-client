package com.open.schedule.activity;

import java.text.SimpleDateFormat;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.open.schedule.storage.tables.ChangableData.Change;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

public abstract class ChangesAdapter extends BaseAdapter {
	protected TreeMap<Long, Change> changes = new TreeMap<Long, Change>();
	protected LayoutInflater inflater;

	protected final SimpleDateFormat timeFormat = new SimpleDateFormat("dd MMMM, yyyy HH:mm");

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