package com.open.schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.SortedMap;

import storage.database.Database;
import storage.tables.Plans;
import storage.tables.Plans.TablePlan;
import storage.tables.Table;
import storage.tables.Table.TableInfo;
import storage.tables.Task;
import utility.Utility;
import io.Client;
import io.Tables;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.widget.DrawerLayout;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	public static final int TIMEOUT_CONNECTION_CHECK = 5000;
	
	public static final String TABLE_ID = "TABLE_ID";

	public static final int REQUEST_NEW_TABLE = 1;
	
	private Integer dayOfMonth;

	private RelativeLayout drawer;
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ListView actionList;

	private ExpandableListView listTablePlans;
	
	private enum Actions {
		ADD_TABLE
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		
		Client.createInstance(new Database(this));

		getActivityElements();

		initDrawer();
		initActions();
		initPlans();

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
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
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
	
	private void getActivityElements() {
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer = (RelativeLayout) findViewById(R.id.drawer);
		drawerList = (ListView) findViewById(R.id.left_drawer);
		actionList = (ListView) findViewById(R.id.action_add);
		listTablePlans = (ExpandableListView) findViewById(R.id.list_tables_plan);
	}

	private void initDrawer() {
		drawerList.setAdapter(new TablesAdapter(Client.getInstance().getTables()));
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
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == Actions.ADD_TABLE.ordinal()) {
					openNewTableActivity();
					return;
				}
				Log.wtf("MainActivity", "Clicked on nonexistent action in listview");
			}
		});
	}
	
	private void initPlans() {
		listTablePlans.setAdapter(new PlansAdapter(getApplicationContext(), Client.getInstance().tables()));
		listTablePlans.setOnChildClickListener(new OnChildClickListener() {			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				PlansAdapter adapter = (PlansAdapter) listTablePlans.getAdapter();
				boolean isSelectable = adapter.isChildSelectable(groupPosition, childPosition);
				if (isSelectable) {
					Integer tableId = adapter.getTableId(groupPosition);
					Integer taskId = adapter.getTaskId(groupPosition, childPosition);
					openTaskActivity(tableId, taskId);					
				}
				return isSelectable;
			}
		});
	}
	
	private void updatePlans() {
		Integer currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		if (this.dayOfMonth != currentDay)
			((PlansAdapter) this.listTablePlans.getAdapter()).update();
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
	
	private void openTaskActivity(Integer tableId, Integer taskId) {
		
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
	
	public class PlansAdapter extends BaseExpandableListAdapter {
		private final Plans plans;
		private final LayoutInflater inflater;

		public PlansAdapter(Context context, Tables tables) {
			this.plans = new Plans(tables);
			this.inflater = LayoutInflater.from(context);
		}
		
		public void update() {
			this.plans.update();
		}
		
		public Integer getTaskId(int groupPosition, int childPosition) {
			TablePlan plan = this.plans.getTodayPlan(groupPosition);
			return plan.tasks.get(childPosition).getId();
		}

		public Integer getTableId(int groupPosition) {
			TablePlan plan = this.plans.getTodayPlan(groupPosition);
			return plan.table.getId();
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return this.plans.getTodayPlan(groupPosition).tasks.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View resultView = convertView;			
			if (resultView == null) {
				resultView = inflater.inflate(android.R.layout.test_list_item, parent);
			}

			TextView taskTitle = (TextView) resultView.findViewById(android.R.id.title);
			Task task = plans.getTodayPlan(groupPosition).tasks.get(childPosition);
			taskTitle.setText(((Task.TaskChange) task.getData()).name);
			return resultView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return this.plans.getTodayPlan(groupPosition).tasks.size();
		}

		@Override
		public TablePlan getGroup(int groupPosition) {
			return this.plans.getTodayPlan(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return this.plans.count();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View theConvertView, ViewGroup parent) {
			View resultView = theConvertView;
			if (resultView == null) {
				resultView = inflater.inflate(android.R.layout.test_list_item, null);
			}

			final TablePlan item = getGroup(groupPosition);
			TextView tableTitle = (TextView) resultView.findViewById(android.R.id.title);
			tableTitle.setText(((Table.TableInfo) item.table.getData()).name);
			return resultView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}
}
