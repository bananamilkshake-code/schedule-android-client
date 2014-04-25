package storage.tables;

import java.util.Collection;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import utility.Utility;

public class ChangableData {

	protected TreeMap<Long,Change> changes = new TreeMap<Long, Change>();
	private long lastUpdate = 0;

	public Entry<Long, Change> getInitial() {
		return changes.firstEntry();
	}
	
	public void change(Change change) {
		Long current_time = Utility.getUnixTime(); 
		changes.put(current_time, change);
		update();
	}

	public Change getData() {
		if (changes.size() == 0)
			return null;

		Change info = changes.lastEntry().getValue();
		
		NavigableMap<Long, Change> reverseChanges = changes.descendingMap();
		Collection<Entry<Long, Change>> set = reverseChanges.entrySet();
		for (Iterator<Entry<Long, Change>> iter = set.iterator(); iter.hasNext() && info.hasNulls();) {
			info.merge(iter.next().getValue());
		}

		return info;
	}

	protected void update() {
		lastUpdate = Utility.getUnixTime();
	}

	public abstract class Change {
		protected Integer creator_id;
		protected long creationTime;

		public Change(Integer creator_id, Long creationTime) {
			this.creator_id = creator_id;
			this.creationTime = creationTime;
		}

		public abstract Boolean hasNulls();
		public abstract void merge(Change prev);
	}
}
