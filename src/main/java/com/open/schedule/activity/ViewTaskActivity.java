package com.open.schedule.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.open.schedule.R;
import com.open.schedule.account.tables.Task;
import com.open.schedule.account.tables.TimeFormat;
import com.open.schedule.utility.Utility;

public class ViewTaskActivity extends ScheduleActivity {
	public static final int REQUEST_CHANGE = 1;

	private Integer tableId;
	private Integer taskId;

	private Task task;

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

		this.tableId = getIntent().getExtras().getInt(ViewTableActivity.EXTRA_TABLE_ID);
		this.taskId = getIntent().getExtras().getInt(ViewTableActivity.EXTRA_TASK_ID);
		this.task = this.getAccount().getTables().get(tableId).getTask(taskId);

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
				return;
			default:
				return;
		}
	}

	private void openChangeTaskActivity() {
		Task.TaskChange data = (Task.TaskChange) task.getData();

		Intent intent = new Intent(ViewTaskActivity.this, EditTaskActivity.class);
		intent.putExtra(EditTaskActivity.EXTRA_CHANGE, true);
		intent.putExtra(EditTaskActivity.EXTRA_NAME, data.name);
		intent.putExtra(EditTaskActivity.EXTRA_DESCRIPTION, data.description);	
		intent.putExtra(EditTaskActivity.EXTRA_START_DATE, Utility.parseToString(data.startDate, TimeFormat.DATE_FORMATTER));
		intent.putExtra(EditTaskActivity.EXTRA_END_DATE, Utility.parseToString(data.endDate, TimeFormat.DATE_FORMATTER));
		intent.putExtra(EditTaskActivity.EXTRA_START_TIME, Utility.parseToString(data.startTime, TimeFormat.TIME_FORMATTER));
		intent.putExtra(EditTaskActivity.EXTRA_END_TIME, Utility.parseToString(data.endTime, TimeFormat.TIME_FORMATTER));
		intent.putExtra(EditTaskActivity.EXTRA_PERIOD, data.period);

		startActivityForResult(intent, REQUEST_CHANGE);
	}

	private class PlaceholderFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_view_task, container, false);

			ViewTaskActivity activity = (ViewTaskActivity) getActivity();
			Task.TaskChange data = (Task.TaskChange) activity.task.getData();

			activity.taskName = (TextView) rootView.findViewById(R.id.view_task_name);
			activity.taskDesc = (TextView) rootView.findViewById(R.id.view_task_desc);
			activity.taskStartDate = (TextView) rootView.findViewById(R.id.view_task_start_date);
			activity.taskEndDate = (TextView) rootView.findViewById(R.id.view_task_date_end);
			activity.taskStartTime = (TextView) rootView.findViewById(R.id.view_task_start_time);
			activity.taskEndTime = (TextView) rootView.findViewById(R.id.view_task_end_time);
			activity.taskPeriod = (TextView) rootView.findViewById(R.id.view_task_period);

			activity.taskName.setText(data.name);
			activity.taskDesc.setText(data.description);
			activity.taskStartDate.setText(Utility.parseToString(data.startDate, TimeFormat.DATE_FORMATTER));
			activity.taskEndDate.setText(Utility.parseToString(data.endDate, TimeFormat.DATE_FORMATTER));
			activity.taskStartTime.setText(Utility.parseToString(data.startTime, TimeFormat.DATE_FORMATTER));
			activity.taskEndTime.setText(Utility.parseToString(data.endTime, TimeFormat.DATE_FORMATTER));
			activity.taskPeriod.setText(data.period.toString());

			return rootView;
		}
	}
}
