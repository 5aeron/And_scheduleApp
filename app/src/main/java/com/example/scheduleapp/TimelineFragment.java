package com.example.scheduleapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;

public class TimelineFragment extends Fragment {

    private RecyclerView timelineRecyclerView;
    private TimelineAdapter timelineAdapter;
    private List<List<TimeSlot>> timeSlotRows;
    private Context context;

    private SharedPreferences prefs;
    private static final String TIMELINE_KEY = "timeline_data";
    private static final String TIMELINE_DATE_KEY = "timeline_last_reset_date";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        context = getContext();

        timelineRecyclerView = view.findViewById(R.id.timeline_recycler_view);
        timelineRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        prefs = requireContext().getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE);
        loadTimelineFromStorage();

        timelineAdapter = new TimelineAdapter(context, timeSlotRows, new TimelineAdapter.OnTimeSlotClickListener() {
            @Override
            public void onTimeSlotClick(TimeSlot slot) {
                Schedule schedule = slot.getSchedule();
                if (schedule != null) {
                    String[] options = {"수정", "삭제"};
                    new AlertDialog.Builder(context)
                            .setTitle("일정 작업 선택")
                            .setItems(options, (dialog, which) -> {
                                if (which == 0) {
                                    showEditScheduleDialog(schedule);
                                } else if (which == 1) {
                                    deleteScheduleFromTimeline(schedule);
                                }
                            })
                            .show();
                }
            }
        });

        timelineRecyclerView.setAdapter(timelineAdapter);

        Button addScheduleButton = view.findViewById(R.id.add_schedule_button);
        if (addScheduleButton != null) {
            addScheduleButton.setOnClickListener(v -> showAddScheduleDialog());
        }

        return view;
    }

    private void saveTimelineToStorage() {
        Gson gson = new Gson();
        String json = gson.toJson(timeSlotRows);
        prefs.edit().putString(TIMELINE_KEY, json).apply();
        prefs.edit().putString(TIMELINE_DATE_KEY, getTodayString()).apply();
    }

    private void loadTimelineFromStorage() {
        String lastDate = prefs.getString(TIMELINE_DATE_KEY, null);
        Calendar now = Calendar.getInstance();
        String today = getTodayString();
        int hour = now.get(Calendar.HOUR_OF_DAY);

        boolean reset = false;
        if (lastDate != null && !today.equals(lastDate) && hour >= 7) {
            reset = true;
        }

        if (reset) {
            timeSlotRows = null;
            initializeTimeSlotsIfEmpty();
            saveTimelineToStorage();
        } else {
            String json = prefs.getString(TIMELINE_KEY, null);
            if (json != null) {
                Type type = new TypeToken<List<List<TimeSlot>>>() {}.getType();
                timeSlotRows = new Gson().fromJson(json, type);

                // 🟩 여기에 아래 코드 추가! (정상 Schedule만 남기고, 나머지는 null 처리)
                for (List<TimeSlot> row : timeSlotRows) {
                    for (TimeSlot slot : row) {
                        Schedule s = slot.getSchedule();
                        if (s != null) {
                            // 예시: 제목이나 시간 값이 완전 이상하면 Schedule을 강제로 null로!
                            // 예: title이 null이거나, blockLength<=0, startHour 범위 벗어나면
                            if (s.getTitle() == null || s.getTitle().isEmpty()
                                    || s.getBlockLength() <= 0
                                    || s.getStartHour() < 8 || s.getStartHour() > 23) {
                                slot.setSchedule(null);
                            }
                        }
                    }
                }
                // ★★★ 일정 객체 통합
                unifySchedulesAfterRestore();
            }
            initializeTimeSlotsIfEmpty();
        }
    }

    // 같은 일정 정보는 동일 Schedule 객체를 참조하도록 통합
    private void unifySchedulesAfterRestore() {
        java.util.Map<Schedule, Schedule> scheduleMap = new java.util.HashMap<>();
        for (List<TimeSlot> row : timeSlotRows) {
            for (TimeSlot slot : row) {
                Schedule s = slot.getSchedule();
                if (s != null) {
                    if (scheduleMap.containsKey(s)) {
                        slot.setSchedule(scheduleMap.get(s));
                    } else {
                        scheduleMap.put(s, s);
                    }
                }
            }
        }
    }

    private void initializeTimeSlotsIfEmpty() {
        if (timeSlotRows == null || timeSlotRows.isEmpty()) {
            timeSlotRows = new ArrayList<>();
            for (int hour = 8; hour < 24; hour++) {
                List<TimeSlot> row = new ArrayList<>();
                for (int min = 0; min < 60; min += 10) {
                    row.add(new TimeSlot(hour, min));
                }
                timeSlotRows.add(row);
            }
        }
    }

    private String getTodayString() {
        Calendar cal = Calendar.getInstance();
        return String.format(Locale.KOREA, "%04d-%02d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
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
        endHourPicker.setMinValue(8);
        endHourPicker.setMaxValue(23);

        NumberPicker endMinutePicker = dialogView.findViewById(R.id.input_end_minute);
        endMinutePicker.setMinValue(0);
        endMinutePicker.setMaxValue(5);

        ToggleButton fixedToggle = dialogView.findViewById(R.id.fixed_toggle);

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
                    saveTimelineToStorage();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    public void deleteScheduleFromTimeline(Schedule target) {
        for (List<TimeSlot> row : timeSlotRows) {
            for (TimeSlot slot : row) {
                Schedule s = slot.getSchedule();
                if (s != null &&
                        s.getTitle().equals(target.getTitle()) &&
                        s.getStartHour() == target.getStartHour() &&
                        s.getStartMinute() == target.getStartMinute() &&
                        s.getBlockLength() == target.getBlockLength() &&
                        s.isFixed() == target.isFixed()
                ) {
                    slot.setSchedule(null);
                }
            }
        }
        timelineAdapter.notifyDataSetChanged();
        saveTimelineToStorage();
    }

    // 일정 수정 다이얼로그
    private void showEditScheduleDialog(Schedule schedule) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_schedule, null);

        EditText titleInput = dialogView.findViewById(R.id.input_title);
        NumberPicker startHourPicker = dialogView.findViewById(R.id.input_start_hour);
        NumberPicker startMinutePicker = dialogView.findViewById(R.id.input_start_minute);
        NumberPicker endHourPicker = dialogView.findViewById(R.id.input_end_hour);
        NumberPicker endMinutePicker = dialogView.findViewById(R.id.input_end_minute);
        ToggleButton fixedToggle = dialogView.findViewById(R.id.fixed_toggle);

        // 기존 일정 정보 세팅
        titleInput.setText(schedule.getTitle());
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
        startHourPicker.setValue(schedule.getStartHour());
        startMinutePicker.setValue(schedule.getStartMinute() / 10);
        int endBlock = (schedule.getStartHour() - 8) * 6 + schedule.getStartMinute() / 10 + schedule.getBlockLength();
        int endHour = 8 + endBlock / 6;
        int endMinute = (endBlock % 6) * 10;
        endHourPicker.setValue(endHour);
        endMinutePicker.setValue(endMinute / 10);
        fixedToggle.setChecked(schedule.isFixed());

        new AlertDialog.Builder(context)
                .setTitle("일정 수정")
                .setView(dialogView)
                .setPositiveButton("수정", (dialog, which) -> {
                    // 기존 일정 삭제
                    deleteScheduleFromTimeline(schedule);

                    // 새 일정 추가 (showAddScheduleDialog와 동일)
                    String title = titleInput.getText().toString().trim();
                    int startHour = startHourPicker.getValue();
                    int startMinute = startMinutePicker.getValue() * 10;
                    int newEndHour = endHourPicker.getValue();
                    int newEndMinute = endMinutePicker.getValue() * 10;
                    boolean isFixed = fixedToggle.isChecked();

                    int startBlock = (startHour - 8) * 6 + startMinute / 10;
                    int newEndBlock = (newEndHour - 8) * 6 + newEndMinute / 10;

                    if (startBlock >= newEndBlock) {
                        Toast.makeText(context, "종료 시간이 시작 시간보다 늦어야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 중복 체크
                    for (int b = startBlock; b < newEndBlock; b++) {
                        int r = b / 6;
                        int c = b % 6;
                        if (timeSlotRows.get(r).get(c).getSchedule() != null) {
                            Toast.makeText(context, "선택한 시간에 이미 일정이 있습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    int blockLength = newEndBlock - startBlock;
                    Schedule newSchedule = new Schedule(title, startHour, startMinute, blockLength,
                            isFixed ? "#C0C0C0" : "#FFB6C1");
                    newSchedule.setFixed(isFixed);

                    for (int b = startBlock; b < newEndBlock; b++) {
                        int r = b / 6;
                        int c = b % 6;
                        timeSlotRows.get(r).get(c).setSchedule(newSchedule);
                    }

                    timelineAdapter.notifyDataSetChanged();
                    saveTimelineToStorage();
                })
                .setNegativeButton("취소", null)
                .show();
    }

}
