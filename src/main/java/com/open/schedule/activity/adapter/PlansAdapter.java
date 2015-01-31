package com.open.schedule.activity.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.open.schedule.R;
import com.open.schedule.account.tables.Plans;
import com.open.schedule.account.tables.Table;
import com.open.schedule.account.tables.Task;
import com.open.schedule.account.tables.TimeFormat;
import com.open.schedule.activity.EditTaskActivity;
import com.open.schedule.activity.MainActivity;
import com.open.schedule.utility.Utility;

public class PlansAdapter extends BaseExpandableListAdapter {
	private final Plans plans;
	private final MainActivity owner;
	private final LayoutInflater inflater;

	public PlansAdapter(MainActivity owner, Plans plans) {
		this.owner = owner;
		this.plans = plans;
		this.inflater = LayoutInflater.from(this.owner.getApplicationContext());
	}

	public void update() {
		this.plans.update();
	}

	public Integer getTaskId(int groupPosition, int childPosition) {
		Plans.TablePlan plan = this.plans.getTodayPlan(groupPosition);
		return plan.tasks.get(childPosition).getId();
	}

	public Integer getTableId(int groupPosition) {
		Plans.TablePlan plan = this.plans.getTodayPlan(groupPosition);
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
	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		Task task = plans.getTodayPlan(groupPosition).tasks.get(childPosition);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_plan, null);
		}

		TextView taskTitle = (TextView) convertView.findViewById(R.id.plan_task_name);
		TextView taskTimeStart = (TextView) convertView.findViewById(R.id.item_task_time_start);
		TextView taskTimeEnd = (TextView) convertView.findViewById(R.id.item_task_time_end);

		Task.TaskChange data = (Task.TaskChange) task.getData();
		taskTitle.setText(data.name);
		taskTimeStart.setText(Utility.parseToString(data.startTime, TimeFormat.TIME_FORMATTER));
		taskTimeEnd.setText(Utility.parseToString(data.endTime, TimeFormat.TIME_FORMATTER));

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Integer tableId = PlansAdapter.this.getTableId(groupPosition);
				Integer taskId = PlansAdapter.this.getTaskId(groupPosition, childPosition);
				PlansAdapter.this.owner.openTaskActivity(tableId, taskId);
			}
		});

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this.plans.getTodayPlan(groupPosition).tasks.size();
	}

	@Override
	public Plans.TablePlan getGroup(int groupPosition) {
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
			resultView = inflater.inflate(R.layout.item_plan_group, null);
		}

		final Plans.TablePlan item = getGroup(groupPosition);
		TextView tableTitle = (TextView) resultView.findViewById(R.id.plan_table_name);
		tableTitle.setText(((Table.TableChange) item.table.getData()).name);
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
