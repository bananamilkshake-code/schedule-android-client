package com.open.schedule;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import storage.tables.Table;
import io.Client;

public class ViewTableActivity extends ActionBarActivity {

	private int tableId;
	private Table table;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_tables);

		tableId = getIntent().getExtras().getInt(MainActivity.TABLE_ID);
		table = Client.getInstance().getTables().get(tableId);

		showTable();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_tables, menu);
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
	
	private void showTable() {
		String name = ((Table.TableInfo)table.getData()).name;
		String description = ((Table.TableInfo)table.getData()).description;

		((TextView) findViewById(R.id.text_table_name)).setText(name);
		((TextView) findViewById(R.id.text_table_description)).setText(description);;

		
	}
}
