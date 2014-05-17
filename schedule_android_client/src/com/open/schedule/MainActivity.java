package com.open.schedule;

import java.util.ArrayList;
import java.util.SortedMap;

import storage.database.Database;
import storage.tables.Table;
import storage.tables.Table.TableInfo;
import utility.Utility;
import io.Client;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.widget.DrawerLayout;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	public static final int TIMEOUT_CONNECTION_CHECK = 5000;
	
	public static final String TABLE_ID = "TABLE_ID";

	public static final int REQUEST_NEW_TABLE = 1;

	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ListView actionList;
	private RelativeLayout drawer;
	
	private enum Actions {
		ADD_TABLE
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Client.createInstance(new Database(this));

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);
		actionList = (ListView) findViewById(R.id.action_add);
		drawer = (RelativeLayout) findViewById(R.id.drawer);
		drawerList.setAdapter(new TablesAdapter(Client.getInstance().getTables()));
		drawerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showTableInfo(position, id);
			}
		});

		String[] actions = getResources().getStringArray(R.array.drawer_elements_bottom);
		ArrayAdapter<String> actionsAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, android.R.id.text1, actions);
		actionList.setAdapter(actionsAdapter);
		actionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == Actions.ADD_TABLE.ordinal()) {
					openNewTableActivity();
					return;
				}
				Log.wtf("MainActivity", "Clicked on nonexistent action in listview");
			}
		});

		new Thread() {
			@Override
			public void run() {
				while(true) {
					while(Client.getInstance().isConnected());
					Client.getInstance().tryConnect();
					if (!Client.getInstance().isConnected()) {
						try {
							Thread.sleep(TIMEOUT_CONNECTION_CHECK);
						} catch (InterruptedException e) {
							Log.e("MainActivity", "Connection sleep", e);
						}
					}
					else {
						Client.getInstance().loadAuthParams();
					}
				}
			}
		}.start();
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
		case R.id.users:
			openUsersActivity();
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
	
	private void createNewTable(Intent data) {
		if (!data.hasExtra(CreateTableActivity.EXTRA_NAME))
			return;
		String name = data.getExtras().getString(CreateTableActivity.EXTRA_NAME);
		String description = data.getExtras().getString(CreateTableActivity.EXTRA_DESCRIPTION);
		Client.getInstance().createTable(true, Client.getInstance().getId(), Utility.getUnixTime(), name, description);
		((BaseAdapter) drawerList.getAdapter()).notifyDataSetChanged();
	}

	private void openLoginActivity() {
		if (Client.getInstance().isConnected()) {
			Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
			startActivity(loginIntent);
		}
		else {
			Toast.makeText(this.getBaseContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
		}
	}

	private void openUsersActivity() {
		Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
		startActivity(usersIntent);
	}
	
	private void openNewTableActivity() {
		Intent newTableIntent = new Intent(MainActivity.this, CreateTableActivity.class);
		startActivityForResult(newTableIntent, REQUEST_NEW_TABLE);
		drawerLayout.closeDrawer(drawer);
	}

	private void openViewTableActivity(Long id) {
		Intent viewTableIntent = new Intent(MainActivity.this, ViewTableActivity.class);
		Integer tableId = Long.valueOf(id).intValue();
		viewTableIntent.putExtra(TABLE_ID, tableId);
		startActivity(viewTableIntent);
	}

	private void showTableInfo(Integer position, long id) {
		drawerList.setItemChecked(position, true);
		openViewTableActivity(id);
		drawerLayout.closeDrawer(drawer);
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
			TextView tableName = (TextView)rowView.findViewById(R.id.item_table_name);
			TextView tableDescription = (TextView)rowView.findViewById(R.id.item_table_description);
			tableName.setText((CharSequence)(((TableInfo)table.getData()).name));
			tableDescription.setText((CharSequence)(((TableInfo)table.getData()).description));
			return rowView;
		}
		
		private void updateTablesIds() {
			idsByPos.clear();
			for (Integer tableId : tables.keySet())
				idsByPos.add(tableId);
		}
	}
}
