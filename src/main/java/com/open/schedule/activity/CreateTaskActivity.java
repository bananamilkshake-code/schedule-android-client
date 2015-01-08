package com.open.schedule.activity;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.open.schedule.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

public class CreateTaskActivity extends ScheduleActivity implements OnClickListener {
	public static final String NAME = "task_name";
	public static final String DESCRIPTION = "task_description";
	public static final String START_DATE = "task_start_date";
	public static final String END_DATE = "task_end_date";
	public static final String START_TIME = "task_start_time";
	public static final String END_TIME = "task_end_time";
	public static final String PERIOD = "task_period";

	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
	public static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.US);
	private static HashMap<Periods, Integer> periodDays = new HashMap<Periods, Integer>();

	static {
		periodDays.put(Periods.ONE_TIME, 1);
		periodDays.put(Periods.WEEK, 7);
		periodDays.put(Periods.THO_WEEKS, 14);
		periodDays.put(Periods.MONTH, 30);
	}
	private EditText name;
	private EditText desc;
	private TextView startDate;
	private TextView endDate;
	private TextView startTime;
	private TextView endTime;
	private Spinner spinnerPeriod;

	private TextView period;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_task);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btCreateTask:
				returnNewTask();
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

	private void returnNewTask() {
		Intent result = new Intent();

		String taskName = name.getText().toString();
		String taskDescription = desc.getText().toString();
		String taskDateStart = startDate.getText().toString();
		String taskDateEnd = endDate.getText().toString();
		String taskTimeStart = startTime.getText().toString();
		String taskTimeEnd = endTime.getText().toString();

		String periodValue;
		if (period.getVisibility() == View.VISIBLE) {
			periodValue = period.getText().toString();
		} else {
			periodValue = periodDays.get(Periods.values()[spinnerPeriod.getSelectedItemPosition()]).toString();
		}

		result.putExtra(NAME, taskName);
		result.putExtra(DESCRIPTION, taskDescription);
		result.putExtra(START_DATE, taskDateStart);
		result.putExtra(END_DATE, taskDateEnd);
		result.putExtra(START_TIME, taskTimeStart);
		result.putExtra(END_TIME, taskTimeEnd);
		result.putExtra(PERIOD, Integer.parseInt(periodValue));

		setResult(RESULT_OK, result);
		finish();
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
		} catch (ParseException e) {
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

	private enum Periods {
		ONE_TIME,
		WEEK,
		THO_WEEKS,
		MONTH,
		OTHER
	}

	public static class PlaceholderFragment extends Fragment {
		public PlaceholderFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_create_task, container, false);
			Button acceptButton = (Button) rootView.findViewById(R.id.btCreateTask);
			acceptButton.setOnClickListener((CreateTaskActivity) getActivity());
			rootView.findViewById(R.id.text_task_date_start).setOnClickListener((CreateTaskActivity) getActivity());
			rootView.findViewById(R.id.text_task_date_end).setOnClickListener((CreateTaskActivity) getActivity());
			rootView.findViewById(R.id.text_task_time_start).setOnClickListener((CreateTaskActivity) getActivity());
			rootView.findViewById(R.id.text_task_time_end).setOnClickListener((CreateTaskActivity) getActivity());

			final CreateTaskActivity activity = (CreateTaskActivity) getActivity();
			activity.name = (EditText) rootView.findViewById(R.id.edit_task_name);
			activity.desc = (EditText) rootView.findViewById(R.id.edit_task_description);
			activity.startDate = (TextView) rootView.findViewById(R.id.text_task_date_start);
			activity.endDate = (TextView) rootView.findViewById(R.id.text_task_date_end);
			activity.startTime = (TextView) rootView.findViewById(R.id.text_task_time_start);
			activity.endTime = (TextView) rootView.findViewById(R.id.text_task_time_end);
			activity.spinnerPeriod = (Spinner) rootView.findViewById(R.id.spinner_period);
			activity.period = (TextView) rootView.findViewById(R.id.edit_period);

			activity.spinnerPeriod.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedView, int position, long id) {
					if (position == CreateTaskActivity.Periods.OTHER.ordinal()) {
						activity.period.setVisibility(View.VISIBLE);
					} else {
						activity.period.setVisibility(View.INVISIBLE);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
				}
			});

			Intent intent = getActivity().getIntent();
			if (intent.hasExtra(CreateTaskActivity.NAME)) {
				activity.name.setText(intent.getStringExtra(CreateTaskActivity.NAME));
				activity.desc.setText(intent.getStringExtra(CreateTaskActivity.DESCRIPTION));
				activity.startDate.setText(intent.getStringExtra(CreateTaskActivity.START_DATE));
				activity.endDate.setText(intent.getStringExtra(CreateTaskActivity.END_DATE));
				activity.startTime.setText(intent.getStringExtra(CreateTaskActivity.START_TIME));
				activity.endTime.setText(intent.getStringExtra(CreateTaskActivity.END_TIME));

				Integer period = intent.getIntExtra(CreateTaskActivity.PERIOD, 1);
				Iterator<Entry<Periods, Integer>> iter = periodDays.entrySet().iterator();
				Boolean found = false;
				while (iter.hasNext()) {
					Entry<Periods, Integer> entry = iter.next();
					if (entry.getValue() != period)
						continue;
					activity.spinnerPeriod.setSelection(entry.getKey().ordinal());
					found = true;
					break;
				}
				if (!found) {
					activity.period.setVisibility(View.VISIBLE);
					activity.period.setText(period);
				}
				acceptButton.setText(R.string.button_task_change);
			}
			return rootView;
		}
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
					textView.setText(String.format("%02d", day) + "." + String.format("%02d", month + 1) + "." + year);
				}
			}, year, monthOfYear, dayOfMonth);
		}
	}
}
