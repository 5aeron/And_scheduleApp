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

public class CalendarFragment extends Fragment {

    private TextView monthText, eventText;
    private ImageView prevMonth, nextMonth;
    private Button addScheduleButton;
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter adapter;

    private Calendar currentCalendar;
    private LocalDate selectedDate;
    private Map<LocalDate, List<Schedule>> scheduleMap = new HashMap<>();

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

    private void updateEventText(LocalDate date) {
        List<Schedule> events = scheduleMap.getOrDefault(date, new ArrayList<>());
        if (events.isEmpty()) {
            eventText.setText("일정 없음");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Schedule s : events) {
                sb.append("• ").append(s.getTitle()).append("\n");
            }
            eventText.setText(sb.toString().trim());
        }
    }

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
                loadCalendar();
                updateEventText(date);
            }
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }
}
