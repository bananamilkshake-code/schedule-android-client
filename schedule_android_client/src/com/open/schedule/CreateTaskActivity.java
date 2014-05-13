package com.open.schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class CreateTaskActivity extends ActionBarActivity implements OnClickListener {
	public static final String NAME = "task_name";
	public static final String DESCRIPTION = "task_description";
	public static final String START_DATE = "task_start_date";
	public static final String END_DATE = "task_end_date";
	public static final String START_TIME = "task_start_time";
	public static final String END_TIME = "task_end_time";

	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
	public static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.US);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_task);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btCreateTask:
			finish();
			return;
		case R.id.text_task_date_start:
		case R.id.text_task_date_end:
			showDatePickerDialog((TextView) view);
			return;
		case R.id.text_task_time_start:
		case R.id.text_task_time_end:
			showTimePickerDialog((TextView) view);
			return;
		default:
			return;
		}
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

	private void showTimePickerDialog(TextView textView) {
		int hour;
		int minute;
		try {
			Date time = CreateTaskActivity.dateFormatter.parse((String) textView.getText());
			Calendar timeValue = new GregorianCalendar();
			timeValue.setTime(time);
			hour = timeValue.get(Calendar.HOUR);
			minute = timeValue.get(Calendar.MINUTE);
		}
		catch (ParseException e) {
			Calendar currentTime = Calendar.getInstance();
			hour = currentTime.get(Calendar.HOUR_OF_DAY);
			minute = currentTime.get(Calendar.MINUTE);
		}
	
		TaskTimePicker timePicker = new TaskTimePicker(CreateTaskActivity.this, hour, minute, true, textView);
		timePicker.setTitle("Select Time");
		timePicker.show();
	}
	
	private void showDatePickerDialog(TextView textView) {
		Calendar currentDate = Calendar.getInstance();
		int year = currentDate.get(Calendar.YEAR);
		int month = currentDate.get(Calendar.MONTH);
		int day = currentDate.get(Calendar.DAY_OF_MONTH);

		TaskDatePicker datePicker = new TaskDatePicker(CreateTaskActivity.this, year, month, day, textView);
		datePicker.setTitle("Select Date");
		datePicker.show();
	}

	private class TaskTimePicker extends TimePickerDialog {
		public TaskTimePicker(Context context, int hourOfDay, int minute, boolean is24HourView, final TextView textView) {
			super(context, new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker timePicker, int hour, int minute) {
					textView.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute));
				}
			}, hourOfDay, minute, is24HourView);
		}
	}

	private class TaskDatePicker extends DatePickerDialog {
		public TaskDatePicker(Context context, int year, int monthOfYear, int dayOfMonth, final TextView textView) {
			super(context, new OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker datePicker, int year, int month, int day) {
					textView.setText(String.format("%02d", day) + "." + String.format("%02d", month) + "." + year);
				}
			}, year, monthOfYear, dayOfMonth);
		}
	}

	public static class PlaceholderFragment extends Fragment {
		public PlaceholderFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_create_task, container, false);
			rootView.findViewById(R.id.btCreateTask).setOnClickListener((CreateTaskActivity)getActivity());
			rootView.findViewById(R.id.text_task_date_start).setOnClickListener((CreateTaskActivity)getActivity());
			rootView.findViewById(R.id.text_task_date_end).setOnClickListener((CreateTaskActivity)getActivity());
			rootView.findViewById(R.id.text_task_time_start).setOnClickListener((CreateTaskActivity)getActivity());
			rootView.findViewById(R.id.text_task_time_end).setOnClickListener((CreateTaskActivity)getActivity());
			return rootView;
		}
	}
}
