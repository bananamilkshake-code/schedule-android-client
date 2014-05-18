package storage.tables;

import io.Client;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

public class Task extends ChangableData {
	private TreeMap<Long, Comment> comments = new TreeMap<Long, Comment>();

	public Task(Integer id) {
		super(id);
	}
	
	public Task(Integer id, Long updateTime) {
		super(id, updateTime);
	}
	
	public Task(Integer id, Integer creatorId, Long time, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime) {
		this(id);
		this.change(time, new TaskChange(creatorId, time, name, description, startDate, endDate, startTime, endTime));
	}

	public void addComment(Integer commentatorId, Long time, String text) {
		comments.put(time, new Comment(commentatorId, text));
	}
	
	public TreeMap<Long, Comment> getComments() {
		return comments;
	}

	public ArrayList<Long> getNewComments(Long logoutTime) {
		ArrayList<Long> commentTimes = new ArrayList<Long>();
		Iterator<Long> commentIter = comments.descendingKeySet().iterator();
		while (commentIter.hasNext()) {
			Long time = commentIter.next();
			if (time >= logoutTime)
				return commentTimes;
			if (comments.get(time).commentatorId != Client.getInstance().getId())
				continue;
			commentTimes.add(time);
		}
		return commentTimes;
	}
	
	public class Comment {
		public Integer commentatorId;
		public String text;

		public Comment(Integer commentatorId, String text) {
			this.commentatorId = commentatorId;
			this.text = text;
		}
	}

	public class TaskChange extends Change {
		public String name = null;
		public String description = null;
		public Date startDate = null;
		public Date endDate = null;
		public Date startTime = null;
		public Date endTime = null;

		public TaskChange(Integer creatorId, Long creationTime, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime) {
			super(creatorId, creationTime);
			this.name = name;
			this.description = description;
			this.startDate = startDate;
			this.endDate = endDate;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		@Override
		public Boolean hasNulls() {
			return (name == null || description == null || startDate == null || endDate == null || startTime == null || endTime == null);
		}

		@Override
		public void merge(Change prev) {
			if(this.name == null)
				this.name = ((TaskChange)prev).name;
			if(this.description == null)
				this.description = ((TaskChange)prev).description;
			if(this.startDate == null)
				this.startDate = ((TaskChange)prev).startDate;
			if(this.endDate == null)
				this.endDate = ((TaskChange)prev).endDate;
			if(this.startTime == null)
				this.startTime = ((TaskChange)prev).startTime;
			if(this.endTime == null)
				this.endTime = ((TaskChange)prev).endTime;
		}
	}
}
