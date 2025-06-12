package com.example.scheduleapp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class TimelineFragment extends Fragment {

    private RecyclerView timelineRecyclerView;
    private TimelineAdapter timelineAdapter;
    private List<List<TimeSlot>> timeSlotRows;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        context = getContext();

        timelineRecyclerView = view.findViewById(R.id.timeline_recycler_view);
        timelineRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        initializeTimeSlots();

        timelineAdapter = new TimelineAdapter(context, timeSlotRows);
        timelineRecyclerView.setAdapter(timelineAdapter);

        Button addScheduleButton = view.findViewById(R.id.add_schedule_button);
        if (addScheduleButton != null) {
            addScheduleButton.setOnClickListener(v -> showAddScheduleDialog());
        }

        return view;
    }

    private void initializeTimeSlots() {
        timeSlotRows = new ArrayList<>();
        for (int hour = 8; hour < 24; hour++) {
            List<TimeSlot> row = new ArrayList<>();
            for (int min = 0; min < 60; min += 10) {
                row.add(new TimeSlot(hour, min));
            }
            timeSlotRows.add(row);
        }
    }

    private void showAddScheduleDialog() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_schedule, null);

        EditText titleInput = dialogView.findViewById(R.id.input_title);

        NumberPicker startHourPicker = dialogView.findViewById(R.id.input_start_hour);
        startHourPicker.setMinValue(8);
        startHourPicker.setMaxValue(23);

        NumberPicker startMinutePicker = dialogView.findViewById(R.id.input_start_minute);
        startMinutePicker.setMinValue(0);
        startMinutePicker.setMaxValue(5);

        NumberPicker endHourPicker = dialogView.findViewById(R.id.input_end_hour);
        startHourPicker.setMinValue(8);
        startHourPicker.setMaxValue(23);

        NumberPicker endMinutePicker = dialogView.findViewById(R.id.input_end_minute);
        endMinutePicker.setMinValue(0);
        endMinutePicker.setMaxValue(5);

        ToggleButton fixedToggle = dialogView.findViewById(R.id.fixed_toggle);

        // Picker 설정
        startHourPicker.setMinValue(8); startHourPicker.setMaxValue(23);
        endHourPicker.setMinValue(8);   endHourPicker.setMaxValue(23);
        startMinutePicker.setMinValue(0); startMinutePicker.setMaxValue(5);
        endMinutePicker.setMinValue(0);   endMinutePicker.setMaxValue(5);

        String[] minuteValues = {"00", "10", "20", "30", "40", "50"};
        startMinutePicker.setDisplayedValues(minuteValues);
        endMinutePicker.setDisplayedValues(minuteValues);

        new AlertDialog.Builder(context)
                .setTitle("일정 추가")
                .setView(dialogView)
                .setPositiveButton("추가", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    int startHour = startHourPicker.getValue();
                    int startMinute = startMinutePicker.getValue() * 10;
                    int endHour = endHourPicker.getValue();
                    int endMinute = endMinutePicker.getValue() * 10;

                    boolean isFixed = fixedToggle.isChecked();

                    int startBlock = (startHour - 8) * 6 + startMinute / 10;
                    int endBlock = (endHour - 8) * 6 + endMinute / 10;

                    if (startBlock >= endBlock) {
                        Toast.makeText(context, "종료 시간이 시작 시간보다 늦어야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 중복 체크
                    for (int b = startBlock; b < endBlock; b++) {
                        int r = b / 6;
                        int c = b % 6;
                        if (timeSlotRows.get(r).get(c).getSchedule() != null) {
                            Toast.makeText(context, "선택한 시간에 이미 일정이 있습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    int blockLength = endBlock - startBlock;
                    Schedule schedule = new Schedule(title, startHour, startMinute, blockLength,
                            isFixed ? "#C0C0C0" : "#FFB6C1");
                    schedule.setFixed(isFixed);

                    for (int b = startBlock; b < endBlock; b++) {
                        int r = b / 6;
                        int c = b % 6;
                        timeSlotRows.get(r).get(c).setSchedule(schedule);
                    }

                    timelineAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
