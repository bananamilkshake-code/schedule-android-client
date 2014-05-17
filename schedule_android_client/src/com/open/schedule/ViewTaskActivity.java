package com.open.schedule;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

import storage.tables.Table;
import storage.tables.Task;
import storage.tables.Task.Comment;
import utility.Utility;
import io.Client;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ViewTaskActivity extends ActionBarActivity {
	private Integer tableId;
	private Integer taskId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_task_comment);

		this.tableId = getIntent().getExtras().getInt(ViewTableActivity.TABLE_ID);
		this.taskId = getIntent().getExtras().getInt(ViewTableActivity.TASK_ID);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	private void addComment(String text) {
		Integer userId = Client.getInstance().getId();
		Client.getInstance().createComment(true, tableId, taskId, Utility.getUnixTime(), userId, text);
	}

	public static class PlaceholderFragment extends Fragment {
		public PlaceholderFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_view_task, container, false);

			ViewTaskActivity activity = (ViewTaskActivity)getActivity();
			final ListView listComments =(ListView) rootView.findViewById(R.id.list_comments);
			Integer tableId = activity.tableId;
			Integer taskId = activity.taskId;
			Table table = Client.getInstance().getTables().get(tableId);
			Task task = table.getTask(taskId); 
			listComments.setAdapter(activity.new CommentsAdapter(task.getComments()));

			rootView.findViewById(R.id.button_add_comment).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					TextView commentText = (TextView) (((View) v.getParent()).findViewById(R.id.edit_comment));
					String text = commentText.getText().toString();
					if (text.length() > 0) {
						((ViewTaskActivity)(getActivity())).addComment(text);
						commentText.setText("");
						((BaseAdapter) listComments.getAdapter()).notifyDataSetChanged();
					}
				}
			});
			return rootView;
		}
	}

	public class CommentsAdapter extends BaseAdapter {
		TreeMap<Long, Comment> comments;

		public CommentsAdapter(TreeMap<Long, Comment> comments) {
			this.comments = comments;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public int getCount() {
			return comments.size();
		}

		@Override
		public Entry<Long, Comment> getItem(int position) {
			return (Entry<Long, Comment>) comments.entrySet().toArray()[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.US);

		@Override
		public View getView(int position, View rowView, ViewGroup arg2) {
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) ViewTaskActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.item_comment, arg2, false);
			}

			TextView commentText = (TextView)rowView.findViewById(R.id.item_comment_text);
			TextView commentTime = (TextView)rowView.findViewById(R.id.item_comment_time);
			TextView commentAuthor = (TextView)rowView.findViewById(R.id.item_comment_author);

			Entry<Long, Comment> comment = getItem(position);

			commentText.setText((CharSequence)(comment.getValue().text));
			commentTime.setText((CharSequence)(dateFormatter.format(comment.getKey())));
			commentAuthor.setText((CharSequence)(comment.getValue().commentatorId.toString()));

			return rowView;
		}
	}
}
