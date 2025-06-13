package com.example.scheduleapp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Calendar;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(TimeSlot slot);
    }

    private final List<List<TimeSlot>> timeSlotRows;
    private final Context context;
    private final OnTimeSlotClickListener listener;

    public TimelineAdapter(Context context, List<List<TimeSlot>> timeSlotRows, OnTimeSlotClickListener listener) {
        this.context = context;
        this.timeSlotRows = timeSlotRows;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline_row, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        final List<TimeSlot> row = timeSlotRows.get(position);
        int hour = row.get(0).getHour();
        holder.hourText.setText(String.format("%02d시", hour));

        final TextView[] slotViews = {
                holder.min0, holder.min10, holder.min20,
                holder.min30, holder.min40, holder.min50
        };

        boolean[] filled = new boolean[6];

        for (int i = 0; i < 6; ) {
            final TimeSlot slot = row.get(i);
            final Schedule schedule = slot.getSchedule();

            if (schedule != null) {
                final int start = i;
                int count = 1;
                for (int j = i + 1; j < 6; j++) {
                    if (row.get(j).getSchedule() == schedule) {
                        count++;
                    } else {
                        break;
                    }
                }

                String title = schedule.getTitle();
                final String[] parts = splitTitleTo3PerSlotNoSpace(title, count);
                final int finalCount = count;

                for (int j = 0; j < finalCount; j++) {
                    final TextView cell = slotViews[start + j];
                    cell.setText(parts[j]);
                    cell.setBackgroundColor(Color.parseColor(schedule.getColor()));
                    cell.setTextColor(Color.BLACK);
                    cell.setPadding(0, 0, 0, 0);

                    cell.setOnClickListener(v -> {
                        if (listener != null) listener.onTimeSlotClick(slot);
                    });

                    filled[start + j] = true;
                }
                i += count;
            } else {
                i++;
            }
        }

        // ★★★ 빈 칸 반드시 초기화 (글씨/배경/클릭리스너 다 초기화!) ★★★
        for (int i = 0; i < 6; i++) {
            if (!filled[i]) {
                slotViews[i].setText("");
                slotViews[i].setBackgroundColor(Color.parseColor("#EEEEEE"));
                slotViews[i].setOnClickListener(null);
            }
        }
    }



    /**
     * 공백 제거, 한 칸에 3글자씩만 분배. 남는 칸은 빈칸
     */
    private String[] splitTitleTo3PerSlotNoSpace(String title, int slotCount) {
        title = title.replace(" ", ""); // 모든 공백 제거
        String[] result = new String[slotCount];
        int len = title.length();
        int idx = 0;
        for (int i = 0; i < slotCount; i++) {
            int remain = len - idx;
            if (remain >= 3) {
                result[i] = title.substring(idx, idx + 3);
                idx += 3;
            } else if (remain > 0) {
                result[i] = title.substring(idx, idx + remain);
                idx += remain;
            } else {
                result[i] = "";
            }
        }
        return result;
    }

    private void showEditDialog(Schedule schedule) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_schedule, null);

        EditText titleInput = dialogView.findViewById(R.id.edit_title);
        titleInput.setText(schedule.getTitle());

        NumberPicker startHourPicker = dialogView.findViewById(R.id.edit_start_hour);
        NumberPicker startMinutePicker = dialogView.findViewById(R.id.edit_start_minute);
        NumberPicker endHourPicker = dialogView.findViewById(R.id.edit_end_hour);
        NumberPicker endMinutePicker = dialogView.findViewById(R.id.edit_end_minute);

        startHourPicker.setMinValue(8);
        startHourPicker.setMaxValue(23);
        endHourPicker.setMinValue(8);
        endHourPicker.setMaxValue(23);
        startMinutePicker.setMinValue(0);
        startMinutePicker.setMaxValue(5);
        endMinutePicker.setMinValue(0);
        endMinutePicker.setMaxValue(5);

        String[] minuteValues = {"00", "10", "20", "30", "40", "50"};
        startMinutePicker.setDisplayedValues(minuteValues);
        endMinutePicker.setDisplayedValues(minuteValues);

        int currentStartHour = -1;
        int currentStartMinute = -1;
        int currentEndHour = -1;
        int currentEndMinute = -1;

        for (int i = 0; i < timeSlotRows.size(); i++) {
            List<TimeSlot> row = timeSlotRows.get(i);
            for (int j = 0; j < row.size(); j++) {
                if (row.get(j).getSchedule() == schedule) {
                    if (currentStartHour == -1) {
                        currentStartHour = row.get(j).getHour();
                        currentStartMinute = row.get(j).getMinute();
                    }
                    currentEndHour = row.get(j).getHour();
                    currentEndMinute = row.get(j).getMinute() + 10;
                    if (currentEndMinute == 60) {
                        currentEndHour++;
                        currentEndMinute = 0;
                    }
                }
            }
        }

        startHourPicker.setValue(currentStartHour);
        startMinutePicker.setValue(currentStartMinute / 10);
        endHourPicker.setValue(currentEndHour);
        endMinutePicker.setValue(currentEndMinute / 10);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("일정 수정/삭제")
                .setView(dialogView)
                .setPositiveButton("수정", null)
                .setNegativeButton("삭제", (d, w) -> {
                    for (List<TimeSlot> row : timeSlotRows) {
                        for (TimeSlot slot : row) {
                            if (slot.getSchedule() == schedule) {
                                slot.setSchedule(null);
                            }
                        }
                    }
                    notifyDataSetChanged();
                })
                .setNeutralButton("취소", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String title = titleInput.getText().toString().trim();
                if (title.isEmpty()) {
                    Toast.makeText(context, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                int startHour = startHourPicker.getValue();
                int startMinute = startMinutePicker.getValue() * 10;
                int endHour = endHourPicker.getValue();
                int endMinute = endMinutePicker.getValue() * 10;

                int startBlock = (startHour - 8) * 6 + startMinute / 10;
                int endBlock = (endHour - 8) * 6 + endMinute / 10;

                if (startBlock >= endBlock) {
                    Toast.makeText(context, "종료 시간이 시작 시간보다 늦어야 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (int b = startBlock; b < endBlock; b++) {
                    int r = b / 6;
                    int c = b % 6;
                    Schedule existingSchedule = timeSlotRows.get(r).get(c).getSchedule();
                    if (existingSchedule != null && existingSchedule != schedule) {
                        Toast.makeText(context, "선택한 시간에 이미 다른 일정이 있습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                for (List<TimeSlot> row : timeSlotRows) {
                    for (TimeSlot slot : row) {
                        if (slot.getSchedule() == schedule) {
                            slot.setSchedule(null);
                        }
                    }
                }

                // 새로운 일정 설정
                int blockLength = endBlock - startBlock;
                Schedule newSchedule = new Schedule(title, startHour, startMinute, blockLength, "#FFB6C1");

                // UI 업데이트
                for (int b = startBlock; b < endBlock; b++) {
                    int r = b / 6;
                    int c = b % 6;
                    timeSlotRows.get(r).get(c).setSchedule(newSchedule);
                }

                notifyDataSetChanged();
                dialog.dismiss();
            });
        });

        dialog.show();
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
