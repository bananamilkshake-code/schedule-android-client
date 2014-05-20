package com.open.schedule;

import io.Client;

import java.util.ArrayList;

import storage.tables.Users;
import storage.tables.Table.Permission;
import storage.tables.Users.User;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UsersActivity extends ActionBarActivity implements OnClickListener {
	public EditText emailText;
	public ListView usersList;

	private Integer tableId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_users);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_search_user:
			searchUser();
			return;
		}
	}
	
	private void searchUser() {
		if (Client.getInstance().isConnected()) {
			DialogFragment users = new UserFragment();
			users.show(getSupportFragmentManager(), getResources().getString(R.string.dialog_title_users));
		}
		else {
			Toast.makeText(this.getBaseContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
		}
	}

	private void changePermission(Integer userId, Permission permission) {
		Client.getInstance().changePermision(true, tableId, userId, permission);		
	}
	
	private void checkPermision(Integer userId) {
		PermissionFragment permissionFragment = new PermissionFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_USERID, userId);
		permissionFragment.setArguments(bundle);
		permissionFragment.show(getSupportFragmentManager(), getResources().getString(R.string.title_permissions));
	}
	
	static final String BUNDLE_USERID = "userId";

	public static class PermissionFragment extends DialogFragment {
		Permission permission;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final UsersActivity activity = (UsersActivity) getActivity();
			final Integer userId = getArguments().getInt(BUNDLE_USERID);
			this.permission = Permission.NONE;
			final String[] permissions = getResources().getStringArray(R.array.permissions);
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setSingleChoiceItems(permissions, permission.ordinal(), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					permission = Permission.values()[which];
				}
			}).setPositiveButton(R.string.button_permission, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					activity.changePermission(userId, permission);
					dialog.dismiss();
				}
			});
			return builder.create();
		}	
	}

	public static class UserFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			UsersActivity activity = (UsersActivity) getActivity();
			final String[] foundUsers = { activity.emailText.getText().toString() };

			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(getResources().getString(R.string.title_found_users)).setItems(foundUsers, new DialogInterface.OnClickListener() {
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
			View rootView = inflater.inflate(R.layout.fragment_users, container, false);
			final UsersActivity activity = (UsersActivity) getActivity();
			activity.emailText = (EditText) rootView.findViewById(R.id.edit_email);
			activity.usersList = (ListView) rootView.findViewById(R.id.list_users);
			rootView.findViewById(R.id.button_search_user).setOnClickListener(activity);
			activity.usersList.setAdapter(activity.new UsersAdapter(Client.getInstance().getUsers()));
			
			Intent intent = getActivity().getIntent();
			if (intent.hasExtra(ReadersActivity.TABLE_ID)) {
				((UsersActivity) getActivity()).tableId = intent.getIntExtra(ReadersActivity.TABLE_ID, 0);
				activity.usersList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View view,	int position, long id) {
						Integer userId = Long.valueOf(id).intValue();
						activity.checkPermision(userId);
					}
				});
			}
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
