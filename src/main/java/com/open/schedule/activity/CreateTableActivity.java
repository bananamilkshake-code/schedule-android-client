package com.open.schedule.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.open.schedule.R;

public class CreateTableActivity extends ScheduleActivity implements OnClickListener {
	public final static String EXTRA_NAME = "Table Name";
	public final static String EXTRA_DESCRIPTION = "Table decription";

	private EditText nameField;
	private EditText descField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_table);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btCreateTable:
				returnNewTable();
				return;
		}
	}

	private void returnNewTable() {
		String tableName = nameField.getText().toString();
		String tableDesc = descField.getText().toString();

		Intent result = new Intent();
		result.putExtra(EXTRA_NAME, tableName);
		result.putExtra(EXTRA_DESCRIPTION, tableDesc);
		setResult(RESULT_OK, result);

		finish();
	}

	public static class PlaceholderFragment extends Fragment {
		public PlaceholderFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_create_table, container, false);

			Button acceptButton = (Button) rootView.findViewById(R.id.btCreateTable);
			CreateTableActivity activity = (CreateTableActivity) getActivity();

			acceptButton.setOnClickListener(activity);

			activity.nameField = (EditText) rootView.findViewById(R.id.editTableName);
			activity.descField = (EditText) rootView.findViewById(R.id.editTableDescription);

			Intent intent = getActivity().getIntent();
			if (intent.hasExtra(ViewTableActivity.TABLE_NAME)) {
				activity.nameField.setText(intent.getStringExtra(ViewTableActivity.TABLE_NAME));
				activity.descField.setText(intent.getStringExtra(ViewTableActivity.TABLE_DESC));

				acceptButton.setText(R.string.button_table_change);
			}

			return rootView;
		}
	}
}
