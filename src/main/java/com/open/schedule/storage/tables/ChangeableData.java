package com.open.schedule.storage.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ChangeableData {
	public TreeMap<Long, Change> changes = new TreeMap<>();

	private Integer id;
	private Integer globalId = null;

	private long lastUpdate = 0;

	public ChangeableData(Integer id) {
		this.id = id;
	}

	public ChangeableData(Integer id, Long updateTime) {
		this(id);
		this.lastUpdate = updateTime;
	}

	public final Integer getId() {
		return this.id;
	}

	public Entry<Long, Change> getInitial() {
		return changes.firstEntry();
	}

	public void change(Long time, Change change) {
		changes.put(time, change);
	}

	public void updateGlobalId(Integer globalId) {
		this.globalId = globalId;
	}

	public Change getData() {
		if (changes.size() == 0)
			return null;

		Change info = changes.lastEntry().getValue();

		NavigableMap<Long, Change> reverseChanges = changes.descendingMap();
		Collection<Entry<Long, Change>> set = reverseChanges.entrySet();
		for (Iterator<Entry<Long, Change>> iter = set.iterator(); iter.hasNext() && info.hasNulls(); )
			info.merge(iter.next().getValue());

		return info;
	}

	public Change getChange(Long time) {
		return changes.get(time);
	}

	public Integer getGlobalId() {
		return this.globalId;
	}

	public ArrayList<Long> getNewChanges(Integer clientId) {
		ArrayList<Long> changes = new ArrayList<Long>();
		Iterator<Long> changeIter = this.changes.descendingKeySet().iterator();
		while (changeIter.hasNext()) {
			Long time = changeIter.next();
			if (time <= this.lastUpdate)
				break;

			if (this.changes.get(time).creatorId != clientId)
				continue;

			changes.add(time);
		}
		return changes;
	}

	public void update(Long time) {
		lastUpdate = time;
	}

	public abstract class Change {
		public final Integer creatorId;

		public final long time;

		public Change(Integer creator_id, Long creationTime) {
			this.creatorId = creator_id;
			this.time = creationTime;
		}

		public abstract Boolean hasNulls();

		public abstract void merge(Change prev);
	}
}
