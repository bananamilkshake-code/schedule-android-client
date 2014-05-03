package com.open.schedule;

import java.util.ArrayList;
import java.util.HashMap;

import storage.database.Database;
import storage.tables.Table;
import storage.tables.Table.TableInfo;
import utility.Utility;
import io.Client;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements OnClickListener {
	public static final int TIMEOUT_CONNECTION_CHECK = 5000;
	
	public static final String TABLE_ID = "TABLE_ID";

	public static final int REQUEST_NEW_TABLE = 1;

	private DrawerLayout drawerLayout;
	private ListView drawerList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Client.createInstance(new Database(this));

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);

		drawerList.setAdapter(new TablesAdapter(Client.getInstance().getTables()));
		drawerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showTableInfo(position);
			}
		});

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}

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
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case REQUEST_NEW_TABLE:
			String name = data.getExtras().getString(CreateTableActivity.EXTRA_NAME);
			String description = data.getExtras().getString(CreateTableActivity.EXTRA_DESCRIPTION);
			Client.getInstance().createTable(Client.getInstance().getId(), true, Utility.getUnixTime(), name, description);
			((BaseAdapter) drawerList.getAdapter()).notifyDataSetChanged();
			return;
		default:
			return;
		}
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.btLogin:
			openLoginActivity();
			return;
		case R.id.btNewTable:
			openNewTableActivity();
			return;
		default:
			return;
		}
	}

	private void openLoginActivity() {
		Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(loginIntent);
	}

	private void openNewTableActivity() {
		Intent newTableIntent = new Intent(MainActivity.this, CreateTableActivity.class);
		startActivityForResult(newTableIntent, REQUEST_NEW_TABLE);
	}

	private void openViewTableActivity(Integer position) {
		Intent viewTableIntent = new Intent(MainActivity.this, ViewTableActivity.class);
		Integer tableId = (int) drawerList.getAdapter().getItemId(position);
		viewTableIntent.putExtra(TABLE_ID, tableId);
		startActivity(viewTableIntent);
	}

	private void showTableInfo(int tableId) {
		drawerList.setItemChecked(tableId, true);
		openViewTableActivity(tableId);
		drawerLayout.closeDrawer(drawerList);
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
			return idsByPos.get(position);
		}

		@Override
		public View getView(int position, View rowView, ViewGroup arg2) {
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		public PlaceholderFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);

			rootView.findViewById(R.id.btLogin).setOnClickListener((MainActivity)getActivity());
			rootView.findViewById(R.id.btNewTable).setOnClickListener((MainActivity)getActivity());

			return rootView;
		}
	}
}
