package com.open.schedule;

import java.util.ArrayList;
import java.util.HashMap;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.os.Bundle;
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
import storage.tables.Table;
import storage.tables.Table.TableInfo;
import io.Client;

public class ViewTablesActivity extends ActionBarActivity implements OnItemClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_tables);

		ListView listView = (ListView) findViewById(R.id.list_tables);

		final TablesAdapter adapter = new TablesAdapter(Client.getInstance().getTables());
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(ViewTablesActivity.this);
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
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (view.getId()) {
		case R.id.list_tables:
			viewTasks(position);
			return;
		default:
			return;
		}
	}

	private void viewTasks(Integer tableId) {
	}

	public class TablesAdapter extends BaseAdapter {
		HashMap<Integer, Table> tables;
		ArrayList<Integer> idsByPos = new ArrayList<Integer>();
		
		public TablesAdapter(HashMap<Integer, Table> tables) {
			this.tables = tables;
			
			for (Integer tableId : tables.keySet()) {
				idsByPos.add(tableId);
			}
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public int getCount() {
			return tables.size();
		}

		@Override
		public Table getItem(int position) {
			return tables.get(idsByPos.get(position));
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View rowView, ViewGroup arg2) {
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) ViewTablesActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.item_table, arg2, false);
			}

			TextView tableName = (TextView)rowView.findViewById(R.id.item_table_name);
			TextView tableDescription = (TextView)rowView.findViewById(R.id.item_table_description);

			Table table = tables.get(idsByPos.get(position));
			tableName.setText((CharSequence)(((TableInfo)table.getData()).name));
			tableDescription.setText((CharSequence)(((TableInfo)table.getData()).description));

			return rowView;
		}
	}
}
