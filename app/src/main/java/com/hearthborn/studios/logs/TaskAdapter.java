package com.hearthborn.studios.logs;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final Context context;
    private final List<Task> tasks;
    private final TaskViewModel taskViewModel;

    public TaskAdapter(Context context, List<Task> tasks, TaskViewModel taskViewModel) {
        this.context = context;
        this.tasks = tasks;
        this.taskViewModel = taskViewModel;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_task_checkbox, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        tasks.clear();
        tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        FrameLayout checkbox;
        ImageView checkmark;
        TextView title;

        TaskViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox);
            checkmark = itemView.findViewById(R.id.checkmark);
            title = itemView.findViewById(R.id.taskTitle);
        }

        void bind(Task task) {
            title.setText(task.getTitle());
            updateUI(task.isCompleted());

            checkbox.setOnClickListener(v -> {
                task.setCompleted(!task.isCompleted());
                taskViewModel.update(task);
                updateUI(task.isCompleted());
            });
        }

        void updateUI(boolean completed) {
            if (completed) {
                checkbox.setBackgroundResource(R.drawable.task_checkbox_checked_bg);
                checkmark.setVisibility(View.VISIBLE);

                title.setTextColor(
                        context.getResources().getColor(R.color.orange)
                );
                title.setPaintFlags(
                        title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                );
            } else {
                checkbox.setBackgroundResource(R.drawable.task_checkbox_bg);
                checkmark.setVisibility(View.GONE);

                title.setTextColor(
                        context.getResources().getColor(R.color.black)
                );
                title.setPaintFlags(
                        title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG
                );
            }
        }
    }
}