package com.example.scheduleapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import android.content.Context;
import android.content.SharedPreferences;

public class CalendarFragment extends Fragment {

    private TextView monthText, eventText;
    private ImageView prevMonth, nextMonth;
    private Button addScheduleButton;
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter adapter;

    private Calendar currentCalendar;
    private LocalDate selectedDate;
    private Map<LocalDate, List<Schedule>> scheduleMap = new HashMap<>();

    private SharedPreferences prefs;
    private static final String SCHEDULE_MAP_KEY = "calendar_schedule_map";

    public CalendarFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // UI 연결
        monthText = view.findViewById(R.id.month_text);
        prevMonth = view.findViewById(R.id.prev_month);
        nextMonth = view.findViewById(R.id.next_month);
        eventText = view.findViewById(R.id.event_text);
        addScheduleButton = view.findViewById(R.id.add_schedule_button);
        calendarRecyclerView = view.findViewById(R.id.calendar_recycler_view);

        currentCalendar = Calendar.getInstance();

        prefs = requireContext().getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE);
        loadSchedulesFromPrefs();

        // 오늘 날짜를 기본 선택
        if (selectedDate == null) {
            selectedDate = LocalDate.now();
        }

        updateMonthText();

        prevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateMonthText();
            loadCalendar();
        });

        nextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateMonthText();
            loadCalendar();
        });

        addScheduleButton.setOnClickListener(v -> {
            if (selectedDate != null) {
                showAddScheduleDialog(selectedDate);
            } else {
                Toast.makeText(getContext(), "날짜를 먼저 선택하세요", Toast.LENGTH_SHORT).show();
            }
        });

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        loadCalendar();
        // 오늘 일정 바로 표시
        updateEventText(selectedDate);

        return view;
    }

    private void updateMonthText() {
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        monthText.setText(year + "년 " + month + "월");
    }

    private void loadCalendar() {
        List<CalendarDay> days = new ArrayList<>();

        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1;
        int maxDay = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int totalCells = firstDayOfWeek + maxDay;
        int displayCells = ((int) Math.ceil(totalCells / 7.0)) * 7;

        tempCal.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek);

        for (int i = 0; i < displayCells; i++) {
            LocalDate date = tempCal.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            boolean isCurrentMonth = tempCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH);
            CalendarDay calendarDay = new CalendarDay(date, isCurrentMonth);

            if (scheduleMap.containsKey(date)) {
                calendarDay.setHasEvent(true);
            }

            days.add(calendarDay);
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        adapter = new CalendarAdapter(days);
        adapter.setSelectedDate(selectedDate);
        adapter.setOnDayClickListener(date -> {
            selectedDate = date;
            adapter.setSelectedDate(date);
            updateEventText(date);
        });

        calendarRecyclerView.setAdapter(adapter);
    }

    // ✔️ 달력 아래 일정 리스트 & 클릭 → 수정/삭제 팝업
    private void updateEventText(LocalDate date) {
        List<Schedule> events = scheduleMap.getOrDefault(date, new ArrayList<>());
        if (events.isEmpty()) {
            eventText.setText("일정 없음");
            eventText.setOnClickListener(null);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < events.size(); i++) {
                sb.append(i + 1).append(". ").append(events.get(i).getTitle()).append("\n");
            }
            eventText.setText(sb.toString().trim());

            eventText.setOnClickListener(v -> showScheduleListDialog(date, events));
        }
    }

    // ✔️ 일정 목록 팝업 (클릭 시 수정/삭제 선택)
    private void showScheduleListDialog(LocalDate date, List<Schedule> events) {
        String[] items = new String[events.size()];
        for (int i = 0; i < events.size(); i++) {
            items[i] = events.get(i).getTitle();
        }
        new AlertDialog.Builder(getContext())
                .setTitle(date + " 일정 선택")
                .setItems(items, (dialog, which) -> {
                    showEditOrDeleteDialog(date, which);
                })
                .show();
    }

    // ✔️ 일정 수정/삭제 선택 다이얼로그
    private void showEditOrDeleteDialog(LocalDate date, int index) {
        String[] options = {"수정", "삭제"};
        new AlertDialog.Builder(getContext())
                .setTitle("일정 작업 선택")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditScheduleDialog(date, index);
                    } else if (which == 1) {
                        deleteSchedule(date, index);
                    }
                })
                .show();
    }

    // ✔️ 일정 수정
    private void showEditScheduleDialog(LocalDate date, int index) {
        List<Schedule> list = scheduleMap.get(date);
        if (list == null || index < 0 || index >= list.size()) return;

        Schedule schedule = list.get(index);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("일정 수정");

        EditText input = new EditText(getContext());
        input.setText(schedule.getTitle());
        builder.setView(input);

        builder.setPositiveButton("수정", (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                schedule.setTitle(newTitle);
                saveSchedulesToPrefs();
                loadCalendar();
                updateEventText(date);
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

    // ✔️ 일정 삭제
    private void deleteSchedule(LocalDate date, int index) {
        List<Schedule> list = scheduleMap.get(date);
        if (list == null || index < 0 || index >= list.size()) return;
        list.remove(index);
        if (list.isEmpty()) {
            scheduleMap.remove(date);
        }
        saveSchedulesToPrefs();
        loadCalendar();
        updateEventText(date);
    }

    // 일정 등록 (기존)
    private void showAddScheduleDialog(LocalDate date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(date + " 일정 추가");

        EditText input = new EditText(getContext());
        input.setHint("할 일 입력");
        builder.setView(input);

        builder.setPositiveButton("등록", (dialog, which) -> {
            String task = input.getText().toString().trim();
            if (!task.isEmpty()) {
                Schedule newSchedule = new Schedule(task, 9, 0, 6, "#FF4081");
                scheduleMap.computeIfAbsent(date, k -> new ArrayList<>()).add(newSchedule);
                saveSchedulesToPrefs();
                loadCalendar();
                updateEventText(date);
            }
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }

    // 데이터 저장 (LocalDate → String으로 변환해서 저장)
    private void saveSchedulesToPrefs() {
        Gson gson = new Gson();
        Map<String, List<Schedule>> stringKeyMap = new HashMap<>();
        for (Map.Entry<LocalDate, List<Schedule>> entry : scheduleMap.entrySet()) {
            stringKeyMap.put(entry.getKey().toString(), entry.getValue());
        }
        String json = gson.toJson(stringKeyMap);
        prefs.edit().putString(SCHEDULE_MAP_KEY, json).apply();
    }

    // 데이터 복원 (String → LocalDate로 역변환)
    private void loadSchedulesFromPrefs() {
        Gson gson = new Gson();
        String json = prefs.getString(SCHEDULE_MAP_KEY, null);
        if (json != null) {
            Type type = new TypeToken<Map<String, List<Schedule>>>(){}.getType();
            try {
                Map<String, List<Schedule>> stringKeyMap = gson.fromJson(json, type);
                scheduleMap = new HashMap<>();
                for (Map.Entry<String, List<Schedule>> entry : stringKeyMap.entrySet()) {
                    scheduleMap.put(LocalDate.parse(entry.getKey()), entry.getValue());
                }
            } catch (Exception e) {
                scheduleMap = new HashMap<>();
            }
        } else {
            scheduleMap = new HashMap<>();
        }
    }
}
