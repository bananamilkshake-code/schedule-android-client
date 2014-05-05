package storage.tables;

import java.util.Collection;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import utility.Utility;

public class ChangableData {

	protected TreeMap<Long, Change> changes = new TreeMap<Long, Change>();
	private long lastUpdate = 0;
	private Integer globalId;

	public Entry<Long, Change> getInitial() {
		return changes.firstEntry();
	}

	public void change(Long time, Change change) {
		changes.put(time, change);
		update();
	}
	
	public void updateglobalId(Integer globalId) {
		this.globalId = globalId;
	}

	public Change getData() {
		if (changes.size() == 0)
			return null;

		Change info = changes.lastEntry().getValue();

		NavigableMap<Long, Change> reverseChanges = changes.descendingMap();
		Collection<Entry<Long, Change>> set = reverseChanges.entrySet();
		for (Iterator<Entry<Long, Change>> iter = set.iterator(); iter.hasNext() && info.hasNulls();)
			info.merge(iter.next().getValue());

		return info;
	}
	
	public Change getChange(Long time) {
		return changes.get(time);
	}
	
	public Integer getGlobalId() {
		return this.globalId;
	}

	protected void update() {
		lastUpdate = Utility.getUnixTime();
	}

	public abstract class Change {
		protected Integer creatorId;
		protected long creationTime;

		public Change(Integer creator_id, Long creationTime) {
			this.creatorId = creator_id;
			this.creationTime = creationTime;
		}

		public abstract Boolean hasNulls();

		public abstract void merge(Change prev);
	}
}
