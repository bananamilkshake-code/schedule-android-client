package com.open.schedule.account.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ChangeableData {
	public TreeMap<Long, Change> changes = new TreeMap<>();

	private Integer id;

	public ChangeableData(Integer id) {
		this.id = id;
	}

	public ChangeableData(Integer id, Long updateTime) {
		this(id);
	}

	public final Integer getId() {
		return this.id;
	}

	public void change(Long time, Change change) {
		changes.put(time, change);
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
