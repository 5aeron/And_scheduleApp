package com.example.scheduleapp;

import android.graphics.Color;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DailyTaskAdapter extends RecyclerView.Adapter<DailyTaskAdapter.ViewHolder> {

    private final List<DailyTask> tasks;
    private final OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onEdit(int position);
        void onDelete(int position);
    }

    public DailyTaskAdapter(List<DailyTask> tasks, OnTaskActionListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyTask task = tasks.get(position);
        String priorityText = (task.getPriority() == 0) ? "" : String.valueOf(task.getPriority());

        holder.priority.setText(priorityText);
        holder.title.setText(task.getTask());
        holder.time.setText(task.getEstimatedTime());

        // 클릭 비활성화: 우선순위 == 0 && 제목 == "할 일을 등록하세요"
        boolean isPlaceholder = (task.getPriority() == 0 && "할 일을 등록하세요".equals(task.getTask()));
        int grayColor = Color.parseColor("#AAAAAA");

        holder.priority.setTextColor(isPlaceholder ? grayColor : Color.BLACK);
        holder.title.setTextColor(isPlaceholder ? grayColor : Color.BLACK);
        holder.time.setTextColor(isPlaceholder ? grayColor : Color.BLACK);

        holder.title.setOnClickListener(isPlaceholder ? null : v -> listener.onEdit(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView priority, title, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            priority = itemView.findViewById(R.id.task_priority);
            title = itemView.findViewById(R.id.task_title);
            time = itemView.findViewById(R.id.task_time);
        }
    }
}
