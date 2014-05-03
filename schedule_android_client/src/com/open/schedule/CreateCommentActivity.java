package com.open.schedule;

import java.text.SimpleDateFormat;
import java.util.TreeSet;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CreateCommentActivity extends ActionBarActivity {
	private Integer tableId;
	private Integer taskId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crearte_comment);

		this.tableId = getIntent().getExtras().getInt(ViewTableActivity.TABLE_ID);
		this.taskId = getIntent().getExtras().getInt(ViewTableActivity.TASK_ID);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.crearte_comment, menu);
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

	private void addComment(String text) {
		Integer userId = Client.getInstance().getId();
		Client.getInstance().createComment(userId, true, tableId, taskId, text, Utility.getUnixTime());
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		
		
		
		public PlaceholderFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_crearte_comment, container, false);

			CreateCommentActivity activity = (CreateCommentActivity)getActivity();
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
						((CreateCommentActivity)(getActivity())).addComment(text);
						commentText.setText("");
						((BaseAdapter) listComments.getAdapter()).notifyDataSetChanged();
					}
				}
			});
			return rootView;
		}
	}

	public class CommentsAdapter extends BaseAdapter {
		TreeSet<Comment> comments;

		public CommentsAdapter(TreeSet<Comment> comments) {
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
		public Comment getItem(int position) {
			return (Comment) comments.toArray()[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm dd-MM-yyyy");
		
		@Override
		public View getView(int position, View rowView, ViewGroup arg2) {
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) CreateCommentActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.item_comment, arg2, false);
			}

			TextView commentText = (TextView)rowView.findViewById(R.id.item_comment_text);
			TextView commentTime = (TextView)rowView.findViewById(R.id.item_comment_time);
			TextView commentAuthor = (TextView)rowView.findViewById(R.id.item_comment_author);

			Comment comment = getItem(position);

			commentText.setText((CharSequence)(comment.text));
			commentTime.setText((CharSequence)(dateFormatter.format(comment.time)));
			commentAuthor.setText((CharSequence)(comment.commentatorId.toString()));

			return rowView;
		}
	}
}
