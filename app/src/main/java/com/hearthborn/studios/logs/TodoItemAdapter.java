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

public class TodoItemAdapter extends RecyclerView.Adapter<TodoItemAdapter.TodoItemViewHolder> {

    private final Context context;
    private final List<TodoItem> todoItems;
    private final TodoViewModel todoViewModel;

    public TodoItemAdapter(Context context, List<TodoItem> todoItems, TodoViewModel todoViewModel) {
        this.context = context;
        this.todoItems = todoItems;
        this.todoViewModel = todoViewModel;
    }

    @NonNull
    @Override
    public TodoItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task_checkbox, parent, false);
        return new TodoItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoItemViewHolder holder, int position) {
        holder.bind(todoItems.get(position));
    }

    @Override
    public int getItemCount() {
        return todoItems.size();
    }

    public void updateItems(List<TodoItem> newItems) {
        todoItems.clear();
        todoItems.addAll(newItems);
        notifyDataSetChanged();
    }

    // Method to get the current list of items
    public List<TodoItem> getItems() {
        return todoItems;
    }

    class TodoItemViewHolder extends RecyclerView.ViewHolder {

        FrameLayout checkbox;
        ImageView checkmark;
        TextView title;

        TodoItemViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox);
            checkmark = itemView.findViewById(R.id.checkmark);
            title = itemView.findViewById(R.id.taskTitle);
        }

        void bind(TodoItem item) {
            title.setText(item.getTitle());
            updateUI(item.isCompleted());

            checkbox.setOnClickListener(v -> {
                item.setCompleted(!item.isCompleted());
                todoViewModel.updateItem(item);
                updateUI(item.isCompleted());
            });
        }

        void updateUI(boolean completed) {
            if (completed) {
                checkbox.setBackgroundResource(R.drawable.task_checkbox_checked_bg);
                checkmark.setVisibility(View.VISIBLE);
                title.setTextColor(context.getResources().getColor(R.color.orange));
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                checkbox.setBackgroundResource(R.drawable.task_checkbox_bg);
                checkmark.setVisibility(View.GONE);
                title.setTextColor(context.getResources().getColor(R.color.black));
                title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
    }
}