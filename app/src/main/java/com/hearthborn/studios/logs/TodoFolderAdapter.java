package com.hearthborn.studios.logs;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TodoFolderAdapter extends RecyclerView.Adapter<TodoFolderAdapter.FolderViewHolder> {

    private final Context context;
    private final List<TodoFolderWithItems> folders;
    private final OnFolderClickListener listener;
    private final TodoViewModel todoViewModel;

    public interface OnFolderClickListener {
        void onFolderClick(TodoFolderWithItems folder);
        void onFolderLongClick(TodoFolderWithItems folder);
    }

    public TodoFolderAdapter(Context context, List<TodoFolderWithItems> folders, TodoViewModel todoViewModel, OnFolderClickListener listener) {
        this.context = context;
        this.folders = new ArrayList<>(folders);
        this.listener = listener;
        this.todoViewModel = todoViewModel;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_todo_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        TodoFolderWithItems folder = folders.get(position);
        holder.bind(folder);
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public void updateFolders(List<TodoFolderWithItems> newFolders) {
        TodoFolderDiffCallback diffCallback = new TodoFolderDiffCallback(this.folders, newFolders);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.folders.clear();
        this.folders.addAll(newFolders);
        diffResult.dispatchUpdatesTo(this);
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(folders, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(folders, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public void saveOrder() {
        for (int i = 0; i < folders.size(); i++) {
            TodoFolderWithItems folder = folders.get(i);
            if (folder.folder.getSortOrder() != i) {
                folder.folder.setSortOrder(i);
                todoViewModel.updateFolder(folder.folder);
            }
        }
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderName, incompleteCount;
        LinearLayout todoPreviewContainer;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            incompleteCount = itemView.findViewById(R.id.incompleteCount);
            todoPreviewContainer = itemView.findViewById(R.id.todoPreviewContainer);
        }

        void bind(TodoFolderWithItems folderWithItems) {
            folderName.setText(folderWithItems.folder.getName());

            int incomplete = 0;
            for (TodoItem item : folderWithItems.items) {
                if (!item.isCompleted()) {
                    incomplete++;
                }
            }
            incompleteCount.setText(String.valueOf(incomplete));

            todoPreviewContainer.removeAllViews();
            int previewCount = Math.min(folderWithItems.items.size(), 3);
            for (int i = 0; i < previewCount; i++) {
                TodoItem item = folderWithItems.items.get(i);
                TextView todoTextView = createTodoPreviewItem(item);
                todoPreviewContainer.addView(todoTextView);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFolderClick(folderWithItems);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onFolderLongClick(folderWithItems);
                }
                return true;
            });
        }

        private TextView createTodoPreviewItem(TodoItem item) {
            TextView textView = new TextView(context);
            textView.setText("• " + item.getTitle());
            textView.setTextSize(14);
            textView.setTextColor(context.getResources().getColor(R.color.text_grey));
            textView.setMaxLines(1);
            textView.setEllipsize(android.text.TextUtils.TruncateAt.END);

            if (item.isCompleted()) {
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            textView.setTypeface(context.getResources().getFont(R.font.inter_regular));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = (int) (4 * context.getResources().getDisplayMetrics().density);
            textView.setLayoutParams(params);

            return textView;
        }
    }

    private static class TodoFolderDiffCallback extends DiffUtil.Callback {
        private final List<TodoFolderWithItems> oldList;
        private final List<TodoFolderWithItems> newList;

        TodoFolderDiffCallback(List<TodoFolderWithItems> oldList, List<TodoFolderWithItems> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).folder.getId().equals(newList.get(newItemPosition).folder.getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
}