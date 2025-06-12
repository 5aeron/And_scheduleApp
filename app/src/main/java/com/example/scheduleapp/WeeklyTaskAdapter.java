package com.example.scheduleapp;

import android.graphics.Paint;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class WeeklyTaskAdapter extends RecyclerView.Adapter<WeeklyTaskAdapter.ViewHolder> {

    private final List<WeeklyTask> tasks;
    private final OnTaskClickListener listener;
    private final OnTaskDeleteListener deleteListener;

    public interface OnTaskClickListener {
        void onTaskClick(int position);
    }

    public interface OnTaskDeleteListener {
        void onDeleteClick(int position);
    }

    public WeeklyTaskAdapter(List<WeeklyTask> tasks, OnTaskClickListener listener, OnTaskDeleteListener deleteListener) {
        this.tasks = tasks;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weekly_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeeklyTask task = tasks.get(position);
        holder.taskTitle.setText(task.getTask());

        String deadline = task.getDeadline();
        String ddayText;
        try {
            LocalDate deadlineDate = LocalDate.parse(deadline, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            long diff = ChronoUnit.DAYS.between(LocalDate.now(), deadlineDate);
            ddayText = (diff == 0) ? "D-day" : (diff > 0 ? "D-" + diff : "D+" + (-diff));
        } catch (Exception e) {
            ddayText = deadline;
        }

        holder.deadline.setText(ddayText);

        if (task.isCompleted()) {
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        boolean isPlaceholder = "할 일을 등록하세요".equals(task.getTask());
        int grayColor = 0xFFAAAAAA;

        holder.taskTitle.setTextColor(isPlaceholder ? grayColor : 0xFF000000);
        holder.deadline.setTextColor(isPlaceholder ? grayColor : 0xFF000000);

        holder.itemView.setOnClickListener(isPlaceholder ? null : v -> listener.onTaskClick(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, deadline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.task_title);
            deadline = itemView.findViewById(R.id.task_deadline);
        }
    }
}
