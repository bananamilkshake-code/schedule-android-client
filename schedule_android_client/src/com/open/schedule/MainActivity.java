package com.open.schedule;

import java.util.ArrayList;
import java.util.HashMap;

import storage.database.Database;
import storage.tables.Table;
import io.Client;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	public static final int REQUEST_NEW_TABLE = 1;

	private ArrayList<String> tablesTitles = new ArrayList<String>();
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Client.createInstance(new Database(this));
		
		HashMap<Integer, Table> tables = Client.getInstance().getTables();
		for (Table table : tables.values()) {
			tablesTitles.add(((Table.TableInfo)table.getData()).name);
		}
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);
		
		drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, (String[]) tablesTitles.toArray(new String[tablesTitles.size()])));
		drawerList.setOnItemClickListener(new DrawerItemClickListener());
		
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
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		
		switch (requestCode) {
		case REQUEST_NEW_TABLE:
			String name = data.getExtras().getString(CreateTableActivity.EXTRA_NAME);
			String description = data.getExtras().getString(CreateTableActivity.EXTRA_DESCRIPTION);
			Client.getInstance().createTable(Client.getInstance().getId(), true, name, description);
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View view) {
		switch(view.getId())
		{
		case R.id.btLogin:
			openLoginActivity();
			return;
		case R.id.btNewTable:
			openNewTableActivity();
			return;
		case R.id.btViewTables:
			openViewTablesActivity();
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
	
	private void openViewTablesActivity() {
		Intent viewTablesIntent = new Intent(MainActivity.this, ViewTablesActivity.class);
		startActivity(viewTablesIntent);
	}
	

	private void selectItem(int position) {
		drawerList.setItemChecked(position, true);
		drawerLayout.closeDrawer(drawerList);
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
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
			rootView.findViewById(R.id.btViewTables).setOnClickListener((MainActivity)getActivity());

			return rootView;
		}
	}
}
