package com.open.schedule.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.open.schedule.R;
import com.open.schedule.account.Account;
import com.open.schedule.account.tables.Table;
import com.open.schedule.activity.adapter.PlansAdapter;

import java.util.ArrayList;
import java.util.SortedMap;

public class MainActivity extends ScheduleActivity {
	public static final String TABLE_ID = "TABLE_ID";

	public static final int REQUEST_NEW_TABLE = 1;

	private RelativeLayout drawer;
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ListView actionList;

	private ExpandableListView listTablePlans;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		this.getActivityElements();

		this.initDrawer();
		this.initActions();
		this.initPlans();
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		updatePlans();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.login:
				openLoginActivity();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
			case REQUEST_NEW_TABLE:
				createNewTable(data);
				return;
			default:
				return;
		}
	}

	private void getActivityElements() {
		this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		this.drawer = (RelativeLayout) findViewById(R.id.drawer);
		this.drawerList = (ListView) findViewById(R.id.left_drawer);
		this.actionList = (ListView) findViewById(R.id.action_add);
		this.listTablePlans = (ExpandableListView) findViewById(R.id.list_tables_plan);
	}

	private void initDrawer() {
		drawerList.setAdapter(new TablesAdapter(this.getAccount().getTables()));
		drawerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showTableInfo(position, id);
			}
		});
	}

	private void initActions() {
		String[] actions = getResources().getStringArray(R.array.drawer_elements_bottom);
		ArrayAdapter<String> actionsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, actions) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView text = (TextView) view.findViewById(android.R.id.text1);
				text.setTextColor(Color.WHITE);
				return view;
			}
		};

		actionList.setAdapter(actionsAdapter);
		actionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == Actions.ADD_TABLE.ordinal()) {
					openNewTableActivity();
					return;
				}

				Log.wtf("MainActivity", "Clicked on nonexistent action in ListView");
			}
		});
	}

	private void initPlans() {
		listTablePlans.setAdapter(new PlansAdapter(this, this.getAccount().getTablePlans()));
	}

	private void updatePlans() {
		((PlansAdapter) listTablePlans.getExpandableListAdapter()).update();
		((PlansAdapter) listTablePlans.getExpandableListAdapter()).notifyDataSetChanged();
	}

	private void createNewTable(Intent data) {
		if (!data.hasExtra(CreateTableActivity.EXTRA_NAME))
			return;

		String name = data.getExtras().getString(CreateTableActivity.EXTRA_NAME);
		String description = data.getExtras().getString(CreateTableActivity.EXTRA_DESCRIPTION);

		Account account = this.getAccount();
		account.createTable(name, description, account.getId(), false);

		((BaseAdapter) drawerList.getAdapter()).notifyDataSetChanged();
	}

	private void openLoginActivity() {
		if (this.isConnected()) {
			Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);

			startActivity(loginIntent);
		} else {
			Toast.makeText(this.getBaseContext(), getResources().getString(R.string.toast_no_connection), Toast.LENGTH_SHORT).show();
		}
	}

	private void openNewTableActivity() {
		Intent newTableIntent = new Intent(MainActivity.this, CreateTableActivity.class);

		startActivityForResult(newTableIntent, REQUEST_NEW_TABLE);

		drawerLayout.closeDrawer(drawer);
	}

	private void openViewTableActivity(Long tableId) {
		Intent viewTableIntent = new Intent(MainActivity.this, ViewTableActivity.class);
		viewTableIntent.putExtra(TABLE_ID, Long.valueOf(tableId).intValue());

		startActivity(viewTableIntent);
	}

	public void openTaskActivity(Integer tableId, Integer taskId) {
		Intent intent = new Intent(MainActivity.this, ViewTaskActivity.class);
		intent.putExtra(ViewTableActivity.TABLE_ID, tableId);
		intent.putExtra(ViewTableActivity.TASK_ID, taskId);

		startActivity(intent);
	}

	private void showTableInfo(Integer position, long id) {
		drawerList.setItemChecked(position, true);

		openViewTableActivity(id);

		drawerLayout.closeDrawer(drawer);
	}

	private enum Actions {
		ADD_TABLE
	}

	public class TablesAdapter extends BaseAdapter {
		SortedMap<Integer, Table> tables;
		ArrayList<Integer> idsByPos = new ArrayList<Integer>();

		public TablesAdapter(SortedMap<Integer, Table> tables) {
			this.tables = tables;
			updateTablesIds();
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
			return idsByPos.get(position);
		}

		@Override
		public View getView(int position, View rowView, ViewGroup arg2) {
			updateTablesIds();

			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.item_table, arg2, false);
			}

			Table table = tables.get(idsByPos.get(position));
			TextView tableName = (TextView) rowView.findViewById(R.id.item_table_name);
			TextView tableDescription = (TextView) rowView.findViewById(R.id.item_table_description);
			tableName.setText(((Table.TableChange) table.getData()).name);
			tableDescription.setText(((Table.TableChange) table.getData()).description);
			return rowView;
		}

		private void updateTablesIds() {
			idsByPos.clear();
			for (Integer tableId : tables.keySet())
				idsByPos.add(tableId);
		}
	}
}
