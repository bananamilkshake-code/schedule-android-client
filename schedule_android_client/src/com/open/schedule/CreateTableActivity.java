package com.open.schedule;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class CreateTableActivity extends ActionBarActivity implements OnClickListener {
	public final static String EXTRA_NAME = "Table Name";
	public final static String EXTRA_DESCRIPTION = "Table decription";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_table);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_table, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_create_table, container, false);
			rootView.findViewById(R.id.btCreateTable).setOnClickListener((CreateTableActivity)getActivity());
			return rootView;
		}
	}
	
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btCreateTable)
			finish();
	}
}
