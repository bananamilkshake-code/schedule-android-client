package storage.tables;

import java.util.Date;
import java.util.TreeSet;
import java.util.Comparator;

public class Task extends ChangableData {
	TreeSet<Comment> comments;
	
	public Task() {
		this.comments = new TreeSet<Comment>(new Comparator<Comment>() {
			public int compare(Comment c1, Comment c2) {
				return (int) (c1.time - c2.time);
			}
		});
	}
	
	public Task(Integer creator_id, Long creationTime, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime) {
		this();
		this.change(new TaskChange(creator_id, creationTime, name, description, startDate, endDate, startTime, endTime));
	}

	public void addComment(Integer commentator_id, Long time, String text) {
		comments.add(new Comment(commentator_id, time, text));
	}
	
	public class Comment {
		public Integer commentator_id;
		public Long time;
		public String text;

		public Comment(Integer commentator_id, Long time, String text) {
			this.commentator_id = commentator_id;
			this.time = time;
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
		
		public TaskChange(Integer creator_id, Long creationTime, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime) {
			super(creator_id, creationTime);
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
