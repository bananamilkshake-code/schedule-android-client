package com.open.schedule;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.support.v7.app.ActionBarActivity;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import storage.database.Database;
import storage.tables.Table;
import storage.tables.Table.TableInfo;
import storage.tables.Task;
import utility.Utility;
import io.Client;

public class ViewTableActivity extends ActionBarActivity implements OnClickListener {

	public final int REQUEST_CREATE_TASK = 1;

	public final String TABLE_ID = "TABLE_ID";

	private Integer tableId;
	private Table table;
	
	private ListView tasksList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_tables);

		tableId = getIntent().getExtras().getInt(MainActivity.TABLE_ID);
		table = Client.getInstance().getTables().get(tableId);

		tasksList = (ListView) findViewById(R.id.list_tasks);
		Button createTask = (Button) findViewById(R.id.btCreateTask);
		showTable();

		createTask.setOnClickListener((OnClickListener) ViewTableActivity.this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.view_tables, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
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
			String name = data.getExtras().getString(CreateTaskActivity.NAME);
			String description = data.getExtras().getString(CreateTaskActivity.DESCRIPTION);
			String startDate = data.getExtras().getString(CreateTaskActivity.START_DATE);
			String endDate = data.getExtras().getString(CreateTaskActivity.END_DATE);
			String startTime = data.getExtras().getString(CreateTaskActivity.START_TIME);
			String endTime = data.getExtras().getString(CreateTaskActivity.END_TIME);

			Date startDateVal = null;
			Date endDateVal = null;
			Date endTimeVal = null;

			try {
				startDateVal = CreateTaskActivity.dateFormatter.parse(startDate);
				endDateVal = CreateTaskActivity.dateFormatter.parse(endDate);
				endTimeVal = CreateTaskActivity.timeFormatter.parse(endTime);
			} catch (ParseException e) {
				Log.w(Database.class.getName(), "Date task changes parsing", e);
			}

			Client.getInstance().createTask(Client.getInstance().getId(), Utility.getUnixTime(), tableId, name, description, startDateVal, endDateVal, endTimeVal);
			((BaseAdapter) tasksList.getAdapter()).notifyDataSetChanged();
			return;
		default:
			return;
		}
	}
	
	private void showTable() {
		String name = ((Table.TableInfo)table.getData()).name;
		String description = ((Table.TableInfo)table.getData()).description;

		((TextView) findViewById(R.id.text_table_name)).setText(name);
		((TextView) findViewById(R.id.text_table_description)).setText(description);

		tasksList.setAdapter(new TasksAdapter(table.getTasks()));
		tasksList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {}
		});
	}

	public void showCreateTaskActivity() {
		Intent intent = new Intent(ViewTableActivity.this, CreateTaskActivity.class);
		startActivityForResult(intent, REQUEST_CREATE_TASK);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btCreateTask:
			showCreateTaskActivity();
			break;
		default:
			break;
		}
	}

	public class TasksAdapter extends BaseAdapter {
		HashMap<Integer, Task> tasks;
		ArrayList<Integer> idsByPos = new ArrayList<Integer>();
		
		public TasksAdapter(HashMap<Integer, Task> tasks) {
			this.tasks = tasks;
			for (Integer taskId : tasks.keySet()) {
				idsByPos.add(taskId);
			}
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
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) ViewTableActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.item_task, arg2, false);
			}

			TextView taskName = (TextView)rowView.findViewById(R.id.item_task_name);
			TextView taskDescription = (TextView)rowView.findViewById(R.id.item_task_description);

			Task task = tasks.get(idsByPos.get(position));

			taskName.setText((CharSequence)(((TableInfo)task.getData()).name));
			taskDescription.setText((CharSequence)(((TableInfo)table.getData()).description));

			return rowView;
		}
	}
}
