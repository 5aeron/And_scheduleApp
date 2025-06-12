package com.example.scheduleapp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    private final List<List<TimeSlot>> timeSlotRows;
    private final Context context;

    public TimelineAdapter(Context context, List<List<TimeSlot>> timeSlotRows) {
        this.context = context;
        this.timeSlotRows = timeSlotRows;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline_row, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        List<TimeSlot> row = timeSlotRows.get(position);
        int hour = row.get(0).getHour();
        holder.hourText.setText(String.format("%02d시", hour));

        TextView[] slotViews = {
                holder.min0, holder.min10, holder.min20,
                holder.min30, holder.min40, holder.min50
        };

        boolean[] filled = new boolean[6]; // 어떤 칸이 채워졌는지 추적

        for (int i = 0; i < 6; i++) {
            TimeSlot slot = row.get(i);
            Schedule schedule = slot.getSchedule();

            if (schedule != null) {
                boolean isFirst = true;

                for (int j = 0; j < 6; j++) {
                    TimeSlot checkSlot = row.get(j);
                    TextView cell = slotViews[j];
                    if (checkSlot.getSchedule() == schedule) {
                        final int finalJ = j;  // lambda-safe
                        if (isFirst) {
                            cell.setText(schedule.getTitle());
                            cell.setBackgroundColor(Color.parseColor(schedule.isFixed() ? "#C0C0C0" : schedule.getColor()));
                            cell.setTextColor(Color.BLACK);
                            isFirst = false;
                        } else {
                            cell.setText("");
                            cell.setBackgroundColor(Color.parseColor(schedule.isFixed() ? "#C0C0C0" : schedule.getColor()));
                        }

                        filled[j] = true;

                        // 첫 번째 셀에만 클릭 이벤트
                        if (finalJ == 0) {
                            cell.setOnClickListener(v -> showEditDialog(schedule));
                        } else {
                            cell.setOnClickListener(null);
                        }
                    }
                }
            }
        }

        // 빈 셀 초기화
        for (int i = 0; i < 6; i++) {
            if (!filled[i]) {
                slotViews[i].setText("");
                slotViews[i].setBackgroundColor(Color.parseColor("#EEEEEE"));
                slotViews[i].setOnClickListener(null);
            }
        }
    }

    private void showEditDialog(Schedule schedule) {
        new AlertDialog.Builder(context)
                .setTitle("일정 수정/삭제")
                .setMessage("이 일정을 수정하거나 삭제하시겠습니까?")
                .setPositiveButton("삭제", (d, w) -> {
                    for (List<TimeSlot> row : timeSlotRows) {
                        for (TimeSlot slot : row) {
                            if (slot.getSchedule() == schedule) {
                                slot.setSchedule(null);
                            }
                        }
                    }
                    notifyDataSetChanged();
                })
                .setNegativeButton("수정", (d, w) -> {
                    Toast.makeText(context, "수정 기능은 추후 구현하세요.", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("취소", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return timeSlotRows.size();
    }

    public static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView hourText, min0, min10, min20, min30, min40, min50;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            hourText = itemView.findViewById(R.id.hourText);
            min0 = itemView.findViewById(R.id.min0);
            min10 = itemView.findViewById(R.id.min10);
            min20 = itemView.findViewById(R.id.min20);
            min30 = itemView.findViewById(R.id.min30);
            min40 = itemView.findViewById(R.id.min40);
            min50 = itemView.findViewById(R.id.min50);
        }
    }
}
