package com.open.schedule;

import storage.database.Database;
import io.Client;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	public static final int REQUEST_NEW_TABLE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		Client.createInstance(new Database(this));

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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

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
