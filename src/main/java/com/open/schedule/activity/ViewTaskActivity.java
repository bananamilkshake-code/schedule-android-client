package com.open.schedule.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.open.schedule.R;
import com.open.schedule.storage.database.Database;
import com.open.schedule.storage.tables.Table;
import com.open.schedule.storage.tables.Task;
import com.open.schedule.storage.tables.Task.Comment;
import com.open.schedule.storage.tables.Task.TaskChange;
import com.open.schedule.utility.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ViewTaskActivity extends ScheduleActivity {
	public static final int REQUEST_CHANGE = 1;

	private Integer tableId;
	private Integer taskId;

	private Task task;

	private ListView changes;

	private TextView taskName;
	private TextView taskDesc;
	private TextView taskStartDate;
	private TextView taskEndDate;
	private TextView taskStartTime;
	private TextView taskEndTime;
	private TextView taskPeriod;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_task);

		this.tableId = getIntent().getExtras().getInt(ViewTableActivity.TABLE_ID);
		this.taskId = getIntent().getExtras().getInt(ViewTableActivity.TASK_ID);
		this.task = this.getClient().getTables().get(tableId).getTask(taskId);

		this.changes = (ListView) findViewById(R.id.list_changes_task);
		this.changes.setAdapter(new TaskChangesAdapted((LayoutInflater) ViewTaskActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE), task));

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.view_task, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.action_change_task:
				this.openChangeTaskActivity();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
			case REQUEST_CHANGE:
				newChange(data);
				return;
			default:
				return;
		}
	}

	private void newChange(Intent data) {
		String name = data.getExtras().getString(CreateTaskActivity.NAME);
		String description = data.getExtras().getString(CreateTaskActivity.DESCRIPTION);
		String startDate = data.getExtras().getString(CreateTaskActivity.START_DATE);
		String endDate = data.getExtras().getString(CreateTaskActivity.END_DATE);
		String startTime = data.getExtras().getString(CreateTaskActivity.START_TIME);
		String endTime = data.getExtras().getString(CreateTaskActivity.END_TIME);
		Integer period = data.getExtras().getInt(CreateTaskActivity.PERIOD);

		Date startDateVal = null;
		Date endDateVal = null;
		Date startTimeVal = null;
		Date endTimeVal = null;

		try {
			startDateVal = CreateTaskActivity.dateFormatter.parse(startDate);
			endDateVal = CreateTaskActivity.dateFormatter.parse(endDate);
			startTimeVal = CreateTaskActivity.timeFormatter.parse(startTime);
			endTimeVal = CreateTaskActivity.timeFormatter.parse(endTime);
		} catch (ParseException e) {
			Log.w(Database.class.getName(), "Date task changes parsing", e);
		}

		this.getClient().changeTask(true, this.tableId, this.taskId, Utility.getUnixTime(), this.getClient().getId(),
				name, description, startDateVal, endDateVal, startTimeVal, endTimeVal, period);

		taskName.setText(name);
		taskDesc.setText(description);
		taskStartDate.setText(startDate);
		taskEndDate.setText(endDate);
		taskStartTime.setText(startTime);
		taskEndTime.setText(endTime);
		taskPeriod.setText(period.toString());
	}

	private void openChangeTaskActivity() {
		Intent intent = new Intent(ViewTaskActivity.this, CreateTaskActivity.class);
		Task.TaskChange data = (Task.TaskChange) task.getData();
		intent.putExtra(CreateTaskActivity.NAME, data.name);
		intent.putExtra(CreateTaskActivity.DESCRIPTION, data.description);
		intent.putExtra(CreateTaskActivity.START_DATE, CreateTaskActivity.dateFormatter.format(data.startDate));
		intent.putExtra(CreateTaskActivity.END_DATE, CreateTaskActivity.dateFormatter.format(data.endDate));
		intent.putExtra(CreateTaskActivity.START_TIME, CreateTaskActivity.timeFormatter.format(data.startTime));
		intent.putExtra(CreateTaskActivity.END_TIME, CreateTaskActivity.timeFormatter.format(data.endTime));
		intent.putExtra(CreateTaskActivity.PERIOD, data.period);
		startActivityForResult(intent, REQUEST_CHANGE);
	}

	private void addComment(String text) {
		Integer userId = this.getClient().getId();
		this.getClient().createComment(true, tableId, taskId, Utility.getUnixTime(), userId, text);
	}

	private class PlaceholderFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_view_task, container, false);

			ViewTaskActivity activity = (ViewTaskActivity) getActivity().getBaseContext();
			Task.TaskChange data = (Task.TaskChange) activity.task.getData();

			activity.taskName = (TextView) rootView.findViewById(R.id.text_task_name);
			activity.taskDesc = (TextView) rootView.findViewById(R.id.text_task_desc);
			activity.taskStartDate = (TextView) rootView.findViewById(R.id.text_task_date_start);
			activity.taskEndDate = (TextView) rootView.findViewById(R.id.text_task_date_end);
			activity.taskStartTime = (TextView) rootView.findViewById(R.id.text_task_time_start);
			activity.taskEndTime = (TextView) rootView.findViewById(R.id.text_task_time_end);
			activity.taskPeriod = (TextView) rootView.findViewById(R.id.text_task_period);

			activity.taskName.setText(data.name);
			activity.taskDesc.setText(data.description);
			activity.taskStartDate.setText(CreateTaskActivity.dateFormatter.format(data.startDate));
			activity.taskEndDate.setText(CreateTaskActivity.dateFormatter.format(data.endDate));
			activity.taskStartTime.setText(CreateTaskActivity.dateFormatter.format(data.startTime));
			activity.taskEndTime.setText(CreateTaskActivity.dateFormatter.format(data.endTime));
			activity.taskPeriod.setText(data.period.toString());

			final ListView listComments = (ListView) rootView.findViewById(R.id.list_comments);
			Integer tableId = activity.tableId;
			Integer taskId = activity.taskId;
			Table table = ViewTaskActivity.this.getClient().getTables().get(tableId);
			Task task = table.getTask(taskId);
			listComments.setAdapter(activity.new CommentsAdapter(task.getComments()));

			rootView.findViewById(R.id.button_add_comment).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					TextView commentText = (TextView) (((View) v.getParent()).findViewById(R.id.edit_comment));
					String text = commentText.getText().toString();
					if (text.length() > 0) {
						((ViewTaskActivity) getActivity()).addComment(text);
						commentText.setText("");
						((BaseAdapter) listComments.getAdapter()).notifyDataSetChanged();
					}
				}
			});
			return rootView;
		}
	}

	public class CommentsAdapter extends BaseAdapter {
		private final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.US);
		TreeMap<Long, Comment> comments;

		public CommentsAdapter(TreeMap<Long, Comment> comments) {
			this.comments = comments;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public int getCount() {
			return comments.size();
		}

		@Override
		public Entry<Long, Comment> getItem(int position) {
			return (Entry<Long, Comment>) comments.entrySet().toArray()[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View rowView, ViewGroup arg2) {
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) ViewTaskActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.item_comment, arg2, false);
			}

			TextView commentText = (TextView) rowView.findViewById(R.id.item_comment_text);
			TextView commentTime = (TextView) rowView.findViewById(R.id.item_comment_time);
			TextView commentAuthor = (TextView) rowView.findViewById(R.id.item_comment_author);

			Entry<Long, Comment> comment = getItem(position);

			commentText.setText((CharSequence) (comment.getValue().text));
			commentTime.setText((CharSequence) (dateFormatter.format(new Date(comment.getKey() * 1000))));
			commentAuthor.setText((CharSequence) (comment.getValue().commentatorId.toString()));

			return rowView;
		}
	}

	private class TaskChangesAdapted extends ChangesAdapter {
		public TaskChangesAdapted(LayoutInflater inflater, Task task) {
			super(inflater, task.changes);
		}

		@Override
		public View getView(int position, View rowView, ViewGroup rootView) {
			if (rowView == null) {
				rowView = this.inflater.inflate(R.layout.item_change_task, rootView, false);
			}

			TaskChange change = (Task.TaskChange) getItem(position);
			TextView author = (TextView) rowView.findViewById(R.id.text_change_table_author);
			TextView time = (TextView) rowView.findViewById(R.id.text_change_table_time);
			TextView name = (TextView) rowView.findViewById(R.id.item_change_table_name);
			TextView desc = (TextView) rowView.findViewById(R.id.item_change_table_desc);
			TextView startDate = (TextView) rowView.findViewById(R.id.item_change_table_start_date);
			TextView endDate = (TextView) rowView.findViewById(R.id.item_change_table_end_date);
			TextView startTime = (TextView) rowView.findViewById(R.id.item_change_table_start_time);
			TextView endTime = (TextView) rowView.findViewById(R.id.item_change_table_end_time);
			TextView period = (TextView) rowView.findViewById(R.id.item_change_task_period);

			author.setText(ViewTaskActivity.this.getClient().getUserName(change.creatorId));
			time.setText(timeFormat.format(new Date(change.time * 1000)));
			name.setText(change.name);
			desc.setText(change.description);
			startDate.setText(CreateTaskActivity.dateFormatter.format(change.startDate));
			endDate.setText(CreateTaskActivity.dateFormatter.format(change.endDate));
			startTime.setText(CreateTaskActivity.timeFormatter.format(change.startTime));
			endTime.setText(CreateTaskActivity.timeFormatter.format(change.endTime));
			period.setText(change.period.toString());

			return rowView;
		}
	}
}
