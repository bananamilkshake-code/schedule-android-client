package storage.tables;

import io.Client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public class ChangableData {

	protected TreeMap<Long, Change> changes = new TreeMap<Long, Change>();
	private long lastUpdate = 0;
	private Integer id;
	private Integer globalId;

	public ChangableData(Integer id) {
		this.id = id;
	}
	
	public ChangableData(Integer id, Long updateTime) {
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
	
	public ArrayList<Long> getChangesAfter() {
		ArrayList<Long> changes = new ArrayList<Long>();
		Iterator<Long> changeIter = this.changes.descendingKeySet().iterator();
		while (changeIter.hasNext()) {
			Long time = changeIter.next();
			if (time <= lastUpdate)
				break;
			if (this.changes.get(time).creatorId != Client.getInstance().getId())
				continue;
			changes.add(time);
		}
		return changes;
	}

	public void update(Long time) {
		lastUpdate = time;
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
