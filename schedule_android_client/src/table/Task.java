package table;

import java.util.Date;
import android.text.format.Time;

public class Task extends ChangableData {
	// TODO: comments list
	
	public Task() {
		
	}

	public class TaskChange extends Change {
		private String name = null;
		private String description = null;
		private Date startDate = null;
		private Date endDate = null;
		private Time endTime = null;
		
		public TaskChange(Integer creator_id) {
			super(creator_id);
		}

		@Override
		public Boolean hasNulls() {
			return (name == null || description == null || startDate == null 
					|| endDate == null || endTime == null);
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
			if(this.endTime == null)
				this.endTime = ((TaskChange)prev).endTime;
		}
	}
}
