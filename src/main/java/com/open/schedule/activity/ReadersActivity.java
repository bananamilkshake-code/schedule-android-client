package com.open.schedule.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.open.schedule.R;

import java.util.Map;
import java.util.Map.Entry;

import com.open.schedule.storage.tables.Table;
import com.open.schedule.storage.tables.Table.Permission;

public class ReadersActivity extends ScheduleActivity {
	private Table table;
	private ListView readersList;

	public static final String TABLE_ID = "tableId";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_readers);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.readers, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_add_user) {
			addNewUser();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void addNewUser() {
		Intent intent = new Intent(ReadersActivity.this, UsersActivity.class);
		intent.putExtra(TABLE_ID, table.getId());
		startActivity(intent);
	}

	public void changePermission(Integer userId, Permission permission) {
		this.getClient().changePermision(true, table.getId(), userId, permission);
		((BaseAdapter)(readersList.getAdapter())).notifyDataSetChanged();
	}

	static final String BUNDLE_USERID = "userId";

	public void choosePermission(Integer userId) {
		PermissionFragment permissionFragment = new PermissionFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_USERID, userId);
		permissionFragment.setArguments(bundle);
		permissionFragment.show(getFragmentManager(), getResources().getString(R.string.title_permissions));
	}

	public static class PermissionFragment extends DialogFragment {
		Permission permission;
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final ReadersActivity activity = (ReadersActivity) getActivity();
			final Integer userId = getArguments().getInt(BUNDLE_USERID);
			this.permission = activity.table.getReaders().get(userId);
			final String[] permissions = getResources().getStringArray(R.array.permissions);
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setSingleChoiceItems(permissions, permission.ordinal(), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					permission = Permission.values()[which];
				}
			}).setPositiveButton(R.string.button_permission, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					activity.changePermission(userId, permission);
					dialog.dismiss();
				}
			});
			return builder.create();
		}	
	}
	
	public class PlaceholderFragment extends Fragment {
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_readers, container, false);

			final ReadersActivity activity = (ReadersActivity) getActivity();
			Intent intent = getActivity().getIntent();
			Integer tableId = intent.getIntExtra(ViewTableActivity.TABLE_ID, 0);
			activity.table = ReadersActivity.this.getClient().getTables().get(tableId);

			activity.readersList = (ListView) rootView.findViewById(R.id.list_readers);
			activity.readersList.setAdapter(activity.new ReadersAdapter(activity.table));
			activity.readersList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
					activity.choosePermission(Long.valueOf(id).intValue());
				}
			});
			return rootView;
		}
	}

	private class ReadersAdapter extends BaseAdapter {
		private Map<Integer, Permission> readers;
		private LayoutInflater inflater = (LayoutInflater) ReadersActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		public ReadersAdapter(Table table) {
			this.readers = table.getReaders();
		}
		
		@Override
		public int getCount() {
			return readers.size();
		}

		@Override
		public Entry<Integer, Permission> getItem(int position) {
			return this.getEntryByPosition(position);
		}

		@Override
		public long getItemId(int position) {
			return getEntryByPosition(position).getKey();
		}

		@Override
		public View getView(int position, View rowView, ViewGroup rootView) {
			if (rowView == null) {
				rowView = this.inflater.inflate(R.layout.item_reader, null);
			}

			Entry<Integer, Permission> entry = this.getItem(position);
			ImageView permissionImage = (ImageView) rowView.findViewById(R.id.image_permission);
			TextView userText = (TextView) rowView.findViewById(R.id.text_reader_name);
			userText.setText(ReadersActivity.this.getClient().getUserName(Long.valueOf(this.getItemId(position)).intValue()));
			
			switch (entry.getValue()) {
			case READ:
				permissionImage.setImageResource(R.drawable.ic_action_edit);
				break;
			case WRITE:
				permissionImage.setImageResource(R.drawable.ic_action_edit);
				break;
			default:
				break;
			}
			
			return rowView;
		}
		
		private Entry<Integer, Permission> getEntryByPosition(Integer position) {
			return this.readers.entrySet().toArray(new Entry[this.readers.size()])[position];
		}
	}
}
