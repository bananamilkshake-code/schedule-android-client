package com.open.schedule;

import java.util.ArrayList;

import storage.tables.Users;
import storage.tables.Users.User;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class UsersActivity extends ActionBarActivity implements OnClickListener {
	public EditText emailText;
	public ListView usersList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_user);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_search_user:
			DialogFragment users = new UserFragment();
			users.show(getSupportFragmentManager(), "Users");
			return;
		}
		
	}

	public static class UserFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			UsersActivity activity = (UsersActivity) getActivity();
			final String[] foundUsers = { activity.emailText.getText().toString() };

			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle("Found users").setItems(foundUsers, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			return builder.create();
		}
	}
	
	public static class PlaceholderFragment extends Fragment {
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_find_user, container, false);
			UsersActivity activity = (UsersActivity) getActivity();
			activity.emailText = (EditText) rootView.findViewById(R.id.edit_email);
			activity.usersList = (ListView) rootView.findViewById(R.id.list_users);
			rootView.findViewById(R.id.button_search_user).setOnClickListener(activity);
			Users users = new Users();
			users.users.put(1, users.new User("John Doe", "example@mail.com"));
			users.users.put(2, users.new User("Alan Smithee", "second@mail.com"));
			users.users.put(3, users.new User("Tommy Atkins", "tommy@mail.com"));
			activity.usersList.setAdapter(activity.new UsersAdapter(users));
			return rootView;
		}
	}

	public class UsersAdapter extends BaseAdapter {
		Users users;
		ArrayList<Integer> idsByPos = new ArrayList<Integer>();
		
		public UsersAdapter(Users users) {
			this.users = users;
			updateTablesIds();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public int getCount() {
			return users.users.size();
		}

		@Override
		public User getItem(int position) {
			return users.users.get(idsByPos.get(position));
		}

		@Override
		public long getItemId(int position) {
			return idsByPos.get(position);
		}

		@Override
		public View getView(int position, View rowView, ViewGroup arg2) {
			updateTablesIds();

			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) UsersActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.item_user, arg2, false);
			}

			User user = users.users.get(idsByPos.get(position));
			TextView tableName = (TextView)rowView.findViewById(R.id.text_name);
			TextView tableDescription = (TextView)rowView.findViewById(R.id.text_email);
			tableName.setText((CharSequence)(user.name));
			tableDescription.setText((CharSequence)(user.email));
			return rowView;
		}
		
		private void updateTablesIds() {
			idsByPos.clear();
			for (Integer userId : users.users.keySet())
				idsByPos.add(userId);
		}
	}
}
