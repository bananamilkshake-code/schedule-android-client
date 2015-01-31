package com.open.schedule.account.tables;

import com.open.schedule.utility.Utility;

import java.util.Date;

public class Task extends ChangeableData {

	private final int tableId;

	public Task(int id, int tableId, String name, String description, int creatorId, Date startDate, Date endDate, Date startTime, Date endTime, Short period) {
		super(id);

		this.tableId = tableId;

		long currentTime = Utility.getUnixTime();
		this.change(currentTime, new TaskChange(creatorId, currentTime, name, description, startDate, endDate, startTime, endTime, period));
	}

	public int getTableId() {
		return  this.tableId;
	}

	public class TaskChange extends Change {
		public String name = null;
		public String description = null;
		public Date startDate = null;
		public Date endDate = null;
		public Date startTime = null;
		public Date endTime = null;
		public Short period = null;

		public TaskChange(Integer creatorId, long time, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime, Short period) {
			super(creatorId, time);
			this.name = name;
			this.description = description;
			this.startDate = startDate;
			this.endDate = endDate;
			this.startTime = startTime;
			this.endTime = endTime;
			this.period = period;
		}

		@Override
		public Boolean hasNulls() {
			return (name == null || description == null || startDate == null || endDate == null || startTime == null || endTime == null || period == null);
		}

		@Override
		public void merge(Change prev) {
			if (this.name == null)
				this.name = ((TaskChange) prev).name;
			if (this.description == null)
				this.description = ((TaskChange) prev).description;
			if (this.startDate == null)
				this.startDate = ((TaskChange) prev).startDate;
			if (this.endDate == null)
				this.endDate = ((TaskChange) prev).endDate;
			if (this.startTime == null)
				this.startTime = ((TaskChange) prev).startTime;
			if (this.endTime == null)
				this.endTime = ((TaskChange) prev).endTime;
			if (this.period == null)
				this.period = ((TaskChange) prev).period;
		}
	}
}
