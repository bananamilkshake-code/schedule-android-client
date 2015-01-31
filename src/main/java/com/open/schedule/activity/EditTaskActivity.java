package com.open.schedule.activity;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.open.schedule.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

public class EditTaskActivity extends ScheduleActivity implements OnClickListener {
	public static final String EXTRA_CHANGE = "TASK_CHANGE";
	public static final String EXTRA_NAME = "TASK_NAME";
	public static final String EXTRA_DESCRIPTION = "TASK_DESCRIPTION";
	public static final String EXTRA_START_DATE = "TASK_START_DATE";
	public static final String EXTRA_END_DATE = "TASK_END_DATE";
	public static final String EXTRA_START_TIME = "TASK_START_TIME";
	public static final String EXTRA_END_TIME = "TASK_END_TIME";
	public static final String EXTRA_PERIOD = "TASK_PERIOD";

	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
	public static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm", Locale.US);

	private Spinner periodSpinner;
	private TextView periodView;

	private static HashMap<Periods, Integer> periodDays = new HashMap<>();

	static {
		periodDays.put(Periods.ONE_TIME, 1);
		periodDays.put(Periods.WEEK, 7);
		periodDays.put(Periods.THO_WEEKS, 14);
		periodDays.put(Periods.MONTH, 30);
	}

	final ArrayList<ValueMapper> valueMappers = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_task);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}

		this.valueMappers.add(new ValueMapper(EXTRA_NAME, R.id.edit_task_name, false));
		this.valueMappers.add(new ValueMapper(EXTRA_DESCRIPTION, R.id.edit_task_description, true));
		this.valueMappers.add(new ValueMapper(EXTRA_START_DATE, R.id.view_task_start_date, true));
		this.valueMappers.add(new ValueMapper(EXTRA_END_DATE, R.id.view_task_date_end, true));
		this.valueMappers.add(new ValueMapper(EXTRA_START_TIME, R.id.view_task_start_time, true));
		this.valueMappers.add(new ValueMapper(EXTRA_END_TIME, R.id.view_task_end_time, true));
		this.valueMappers.add(new ValueMapper(EXTRA_PERIOD, R.id.edit_task_period, true));
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.button_create_task:
				returnNewTask();
				return;
			case R.id.view_task_start_date:
			case R.id.view_task_date_end:
				showDatePickerDialog((TextView) view);
				return;
			case R.id.view_task_start_time:
			case R.id.view_task_end_time:
				showTimePickerDialog((TextView) view);
				return;
			default:
				return;
		}
	}
	private void returnNewTask() {
		if (!this.checkValues()) {
			return;
		}

		Intent result = new Intent();

		for (ValueMapper mapper : valueMappers) {
			String newValue = mapper.view.getText().toString();

			if (mapper.oldValue == null || newValue.compareTo(mapper.oldValue) == 0) {
				result.putExtra(mapper.intentExtra, newValue);
			} else {
				result.putExtra(mapper.intentExtra, (String) null);
			}
		}

		setResult(RESULT_OK, result);
		finish();
	}

	public boolean checkValues() {
		for (ValueMapper mapper : valueMappers) {
			if (mapper.canBeEmpty)
				continue;

			mapper.view.setError(null);

			String value = mapper.view.getText().toString();
			if (!TextUtils.isEmpty(value)) {
				continue;
			}

			mapper.view.setError(this.getString(R.string.error_task_name_is_empty));
			mapper.view.requestFocus();

			return false;
		}

		return true;
	}

	private void showTimePickerDialog(TextView textView) {
		int hour;
		int minute;

		try {
			Date time = EditTaskActivity.DATE_FORMATTER.parse((String) textView.getText());
			Calendar timeValue = new GregorianCalendar();
			timeValue.setTime(time);

			hour = timeValue.get(Calendar.HOUR);
			minute = timeValue.get(Calendar.MINUTE);
		} catch (ParseException e) {
			Calendar currentTime = Calendar.getInstance();

			hour = currentTime.get(Calendar.HOUR_OF_DAY);
			minute = currentTime.get(Calendar.MINUTE);
		}

		TaskTimePicker timePicker = new TaskTimePicker(EditTaskActivity.this, hour, minute, true, textView);
		timePicker.setTitle(this.getString(R.string.time_picker_time));
		timePicker.show();
	}

	private void showDatePickerDialog(TextView textView) {
		Calendar currentDate = Calendar.getInstance();
		int year = currentDate.get(Calendar.YEAR);
		int month = currentDate.get(Calendar.MONTH);
		int day = currentDate.get(Calendar.DAY_OF_MONTH);

		TaskDatePicker datePicker = new TaskDatePicker(EditTaskActivity.this, year, month, day, textView);
		datePicker.setTitle(this.getString(R.string.time_picker_date));
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
			Button acceptButton = (Button) rootView.findViewById(R.id.button_create_task);

			final EditTaskActivity activity = (EditTaskActivity) this.getActivity();

			for (ValueMapper mapper : activity.valueMappers) {
				mapper.view = (TextView) rootView.findViewById(mapper.viewId);
			}

			activity.periodView = (TextView) rootView.findViewById(R.id.edit_task_period);
			activity.periodSpinner = (Spinner) rootView.findViewById(R.id.edit_task_spinner_period);

			acceptButton.setOnClickListener(activity);
			rootView.findViewById(R.id.view_task_start_date).setOnClickListener(activity);
			rootView.findViewById(R.id.view_task_date_end).setOnClickListener(activity);
			rootView.findViewById(R.id.view_task_start_time).setOnClickListener(activity);
			rootView.findViewById(R.id.view_task_end_time).setOnClickListener(activity);

			activity.periodSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedView, int position, long id) {
					if (position == EditTaskActivity.Periods.OTHER.ordinal()) {
						activity.periodView.setVisibility(View.VISIBLE);
						activity.periodView.setText(null);
					} else {
						activity.periodView.setVisibility(View.INVISIBLE);
						activity.periodView.setText(periodDays.get(Periods.values()[activity.periodSpinner.getSelectedItemPosition()]).toString());
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
				}
			});

			Intent intent = getActivity().getIntent();
			if (intent.getBooleanExtra(EditTaskActivity.EXTRA_NAME, false)) {
				for (ValueMapper mapper : activity.valueMappers) {
					mapper.view.setText(intent.getStringExtra(mapper.intentExtra));
				}

				Integer period = Integer.valueOf(intent.getStringExtra(EditTaskActivity.EXTRA_PERIOD));
				Iterator<Entry<Periods, Integer>> iter = periodDays.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Periods, Integer> entry = iter.next();
					if (entry.getValue() != period)
						continue;

					activity.periodSpinner.setSelection(entry.getKey().ordinal());
					activity.periodView.setVisibility(View.VISIBLE);
					activity.periodView.setText(period);
					break;
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
