package com.open.schedule;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CreateTaskActivity extends ActionBarActivity implements OnClickListener {
	public static final String NAME = "task_name";
	public static final String DESCRIPTION = "task_description";
	public static final String START_DATE = "task_start_date";
	public static final String END_DATE = "task_end_date";
	public static final String START_TIME = "task_start_time";
	public static final String END_TIME = "task_end_time";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_task);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.create_task, menu);
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
	public void onClick(View view) {
		if (view.getId() == R.id.btCreateTask)
			finish();
	}

	@Override
	public void finish() {
		Intent result = new Intent();
		
		String taskName = ((EditText)findViewById(R.id.edit_task_name)).getText().toString();
		String taskDescription = ((EditText)findViewById(R.id.edit_task_description)).getText().toString();
		String taskDateStart = ((TextView)findViewById(R.id.text_task_date_start)).getText().toString();
		String taskDateEnd = ((TextView)findViewById(R.id.text_task_date_end)).getText().toString();
		String taskTimeStart = ((TextView)findViewById(R.id.text_task_time_start)).getText().toString();
		String taskTimeEnd = ((TextView)findViewById(R.id.text_task_time_end)).getText().toString();
		
		result.putExtra(NAME, taskName);
		result.putExtra(DESCRIPTION, taskDescription);
		result.putExtra(START_DATE, taskDateStart);
		result.putExtra(END_DATE, taskDateEnd);
		result.putExtra(START_TIME, taskTimeStart);
		result.putExtra(END_TIME, taskTimeEnd);

		setResult(RESULT_OK, result);
		super.finish();
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_create_task, container, false);
			rootView.findViewById(R.id.btCreateTask).setOnClickListener((CreateTaskActivity)getActivity());
			return rootView;
		}
	}

}
