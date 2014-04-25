package com.open.schedule;

import java.util.ArrayList;
import java.util.HashMap;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import storage.tables.Table;
import storage.tables.Table.TableInfo;
import io.Client;

public class ViewTablesActivity extends ActionBarActivity implements OnItemClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_tables);

		if (savedInstanceState == null)
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
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
		case R.id.listViewTables:
			viewTasks(position);
			return;
		default:
			return;
		}
	}

	private void viewTasks(Integer tableId) {
		
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_view_tables, container, false);

			ListView listView = (ListView) rootView.findViewById(R.id.listViewTables);
			final ArrayList<String> list = new ArrayList<String>();
			HashMap<Integer, Table> tables = Client.getInstance().getTables();
			
			for (Table value : tables.values())
				list.add(((TableInfo) value.getData()).name);
			
			final TablesAdapter adapter = new TablesAdapter((ViewTablesActivity)getActivity(), R.layout.fragment_view_tables, list);
			listView.setAdapter(adapter);
			
			listView.setOnItemClickListener((ViewTablesActivity) getActivity());
			fillTables(listView);

			return rootView;
		}
		
		private void fillTables(ListView tablesView) {
		}

		private class TablesAdapter extends ArrayAdapter<String> {
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			
			public TablesAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
				super(context, textViewResourceId, objects);
				for (int i = 0; i < objects.size(); ++i) 
					map.put(objects.get(i), i);
			}
			
			@Override
			public long getItemId(int position) {
				String item = getItem(position);
				return map.get(item);
			}
			
			@Override
			public boolean hasStableIds() {
				return true;
			}
		}
	}
}
