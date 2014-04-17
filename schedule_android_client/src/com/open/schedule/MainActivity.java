package com.open.schedule;

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}

		new Thread() {
			@Override
			public void run() {
				while(true)
				{
					while(Client.getInstance().isConnected()); // Yuck!

					Client.getInstance().tryConnect();

					if (!Client.getInstance().isConnected()) // Double Yuck!
					{
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
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

			return rootView;
		}
	}
	
	@Override
	public void onClick(View view) {
		switch(view.getId())
		{
		case R.id.btLogin:
			openLoginActivity();
			break;
		case R.id.btNewTable:
			openNewTableActivity();
			break;
		default:
			break;
		}
	}
	
	private void openLoginActivity() {
		Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(loginIntent);
	}
	
	private void openNewTableActivity() {
		Intent newTableIntent = new Intent(MainActivity.this, CreateTableActivity.class);
		startActivity(newTableIntent);
	}
}
