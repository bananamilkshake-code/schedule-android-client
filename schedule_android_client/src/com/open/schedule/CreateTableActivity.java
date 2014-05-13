package com.open.schedule;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class CreateTableActivity extends ActionBarActivity implements OnClickListener {
	public final static String EXTRA_NAME = "Table Name";
	public final static String EXTRA_DESCRIPTION = "Table decription";

	private EditText nameField;
	private EditText descField;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_table);

		nameField = (EditText) findViewById(R.id.editTableName);
		descField = (EditText)findViewById(R.id.editTableDescription);
		
		Intent intent = getIntent();
		if (intent.hasExtra(ViewTableActivity.TABLE_NAME)) {
			nameField.setText(intent.getStringExtra(ViewTableActivity.TABLE_NAME));
			descField.setText(intent.getStringExtra(ViewTableActivity.TABLE_DESC));
		}
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
	
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btCreateTable)
			finish();
	}
	
	@Override
	public void finish() {
		Intent result = new Intent();
		
		String tableName = ((EditText)findViewById(R.id.editTableName)).getText().toString();
		String tableDesc = ((EditText)findViewById(R.id.editTableDescription)).getText().toString();

		result.putExtra(EXTRA_NAME, tableName);
		result.putExtra(EXTRA_DESCRIPTION, tableDesc);

		setResult(RESULT_OK, result);
		super.finish();
	}

	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_create_table, container, false);
			rootView.findViewById(R.id.btCreateTable).setOnClickListener((CreateTableActivity)getActivity());
			return rootView;
		}
	}
}
