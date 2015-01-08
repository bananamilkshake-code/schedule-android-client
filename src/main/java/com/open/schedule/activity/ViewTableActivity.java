package com.open.schedule.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.open.schedule.R;
import com.open.schedule.account.Account;
import com.open.schedule.account.tables.Table;
import com.open.schedule.account.tables.Task;
import com.open.schedule.account.tables.Task.TaskChange;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.SortedMap;

public class ViewTableActivity extends ScheduleActivity {
	private static final String LOG_TAG = ViewTableActivity.class.getName();

	public static final int REQUEST_CREATE_TASK = 1;
	public static final int REQUEST_CHANGE = 2;

	public static final String TABLE_ID = "tableId";
	public static final String TASK_ID = "taskId";
	public static final String TABLE_NAME = "name";
	public static final String TABLE_DESC = "desc";

	private Integer tableId;
	private Table table;

	private ListView tasksList;

	private TextView tableName;
	private TextView tableDesc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_table);

		this.tableId = getIntent().getExtras().getInt(MainActivity.TABLE_ID);
		this.table = this.getAccount().getTables().get(tableId);

		this.tableName = (TextView) findViewById(R.id.text_table_name);
		this.tableDesc = ((TextView) findViewById(R.id.text_table_description));

		showTable();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.view_tables, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.action_change:
				showChangeTableActivity();
				return true;
			case R.id.action_add_task:
				showCreateTaskActivity();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
			case REQUEST_CREATE_TASK:
				newTask(data);
				return;
			default:
				return;
		}
	}

	private void newTask(Intent data) {
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
			Log.w(LOG_TAG, "Date task changes parsing", e);
		}

		final Account account = this.getAccount();
		account.createTask(this.tableId, name, description, account.getId(), startDateVal, endDateVal, startTimeVal, endTimeVal, period, true);

		((BaseAdapter) tasksList.getAdapter()).notifyDataSetChanged();
	}

	private void showTable() {
		String name = ((Table.TableChange) table.getData()).name;
		String description = ((Table.TableChange) table.getData()).description;

		this.tableName.setText(name);
		this.tableDesc.setText(description);

		tasksList = (ListView) findViewById(R.id.list_tasks);
		tasksList.setAdapter(new TasksAdapter(table.getTasks()));
		tasksList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showTask(Long.valueOf(id).intValue());
			}
		});
	}

	private void showTask(Integer taskId) {
		Intent intent = new Intent(ViewTableActivity.this, ViewTaskActivity.class);
		intent.putExtra(TABLE_ID, this.tableId);
		intent.putExtra(TASK_ID, taskId);
		startActivity(intent);
	}

	private void showChangeTableActivity() {
		Intent intent = new Intent(ViewTableActivity.this, CreateTableActivity.class);
		intent.putExtra(TABLE_NAME, ((Table.TableChange) table.getData()).name);
		intent.putExtra(TABLE_DESC, ((Table.TableChange) table.getData()).description);
		startActivityForResult(intent, REQUEST_CHANGE);
	}

	private void showCreateTaskActivity() {
		Intent intent = new Intent(ViewTableActivity.this, CreateTaskActivity.class);
		startActivityForResult(intent, REQUEST_CREATE_TASK);
	}

	public class TasksAdapter extends BaseAdapter {
		private SortedMap<Integer, Task> tasks;
		private ArrayList<Integer> idsByPos = new ArrayList<Integer>();

		public TasksAdapter(SortedMap<Integer, Task> tasks) {
			this.tasks = tasks;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public int getCount() {
			return tasks.size();
		}

		@Override
		public Task getItem(int position) {
			return tasks.get(idsByPos.get(position));
		}

		@Override
		public long getItemId(int position) {
			return idsByPos.get(position);
		}

		@Override
		public View getView(int position, View rowView, ViewGroup arg2) {
			idsByPos.clear();
			for (Integer taskId : tasks.keySet()) {
				idsByPos.add(taskId);
			}

			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) ViewTableActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.item_task, arg2, false);
			}

			Task task = tasks.get(idsByPos.get(position));
			TaskChange data = (TaskChange) task.getData();
			TextView taskName = (TextView) rowView.findViewById(R.id.item_task_name);
			TextView taskDescription = (TextView) rowView.findViewById(R.id.item_task_description);
			TextView taskStartDate = (TextView) rowView.findViewById(R.id.item_task_date_start);
			TextView taskEndDate = (TextView) rowView.findViewById(R.id.item_task_date_end);
			TextView taskStartTime = (TextView) rowView.findViewById(R.id.item_task_time_start);
			TextView taskEndTime = (TextView) rowView.findViewById(R.id.item_task_time_end);
			TextView taskPeriod = (TextView) rowView.findViewById(R.id.item_task_period);

			taskName.setText((CharSequence) (data.name));
			taskDescription.setText((CharSequence) (data.description));
			taskStartDate.setText((CharSequence) (CreateTaskActivity.dateFormatter.format(data.startDate)));
			taskEndDate.setText((CharSequence) (CreateTaskActivity.dateFormatter.format(data.endDate)));
			taskStartTime.setText((CharSequence) (CreateTaskActivity.timeFormatter.format(data.startTime)));
			taskEndTime.setText((CharSequence) (CreateTaskActivity.timeFormatter.format(data.endTime)));
			taskPeriod.setText((CharSequence) (data.period.toString()));

			return rowView;
		}
	}
}
