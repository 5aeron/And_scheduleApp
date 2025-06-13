package com.example.scheduleapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TodoFragment extends Fragment {

    private RecyclerView dailyRecyclerView, weeklyRecyclerView;
    private DailyTaskAdapter dailyAdapter;
    private WeeklyTaskAdapter weeklyAdapter;
    private List<DailyTask> dailyTasks = new ArrayList<>();
    private List<WeeklyTask> weeklyTasks = new ArrayList<>();
    private TextView dateText, ddayText, weeklyRangeTextView;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "schedule_prefs";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo, container, false);

        dailyRecyclerView = view.findViewById(R.id.daily_recycler_view);
        weeklyRecyclerView = view.findViewById(R.id.weekly_recycler_view);
        dateText = view.findViewById(R.id.date_text);
        ddayText = view.findViewById(R.id.dday_text);
        weeklyRangeTextView = view.findViewById(R.id.weekly_range_text);
        view.findViewById(R.id.add_daily_button).setOnClickListener(v -> showAddDailyDialog());
        view.findViewById(R.id.add_weekly_button).setOnClickListener(v -> showAddWeeklyDialog());

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        updateTodayAndDdayText();
        updateWeeklyRangeText();
        ddayText.setOnClickListener(v -> showDdayPicker());

        loadTasksFromStorage();

        setupDailyList();
        setupWeeklyList();

        sortDailyTasks();
        sortWeeklyTasks();

        return view;
    }

    private void sortDailyTasks() {
        Collections.sort(dailyTasks, Comparator
                .comparingInt(DailyTask::getPriority)
                .thenComparingInt(DailyTask::getEstimatedMinutes));
    }

    private void sortWeeklyTasks() {
        Collections.sort(weeklyTasks, Comparator.comparing(task -> {
            try {
                return LocalDate.parse(task.getDeadline());
            } catch (Exception e) {
                return LocalDate.MAX;
            }
        }));
    }

    private void updateTodayAndDdayText() {
        LocalDate today = LocalDate.now();
        dateText.setText(today.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")));

        String saved = prefs.getString("dday_target", null);
        if (saved != null) {
            LocalDate targetDate = LocalDate.parse(saved);
            long dday = ChronoUnit.DAYS.between(today, targetDate);
            if (dday >= 0) {
                ddayText.setText("D-" + dday);
            } else {
                ddayText.setText("종료");
            }
        } else {
            ddayText.setText("D-day 없음");
        }
    }

    private void updateWeeklyRangeText() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        String range = startOfWeek.format(DateTimeFormatter.ofPattern("M월 d일"))
                + " ~ " + endOfWeek.format(DateTimeFormatter.ofPattern("M월 d일"));
        weeklyRangeTextView.setText("Weekly (" + range + ")");
    }

    private void loadTasksFromStorage() {
        Gson gson = new Gson();

        // 1. 마지막 저장 날짜 불러오기
        String lastDateStr = prefs.getString("last_daily_date", null);

        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        boolean resetDaily = false;
        if (lastDateStr != null) {
            LocalDate lastDate = LocalDate.parse(lastDateStr);
            // 오늘 날짜와 다르고, 현재 시간이 7시 이후라면 초기화
            if (!today.equals(lastDate) && nowTime.isAfter(LocalTime.of(7, 0))) {
                resetDaily = true;
            }
        }

        // 2. Daily Tasks 불러오기/초기화
        if (resetDaily) {
            dailyTasks = new ArrayList<>(); // 초기화
            saveLastDailyDate(today);       // 날짜 갱신
            saveTasksToStorage();           // 비운 걸 바로 저장
        } else {
            String dailyJson = prefs.getString("daily_tasks", null);
            if (dailyJson != null) {
                Type type = new TypeToken<List<DailyTask>>(){}.getType();
                dailyTasks = gson.fromJson(dailyJson, type);
            } else {
                dailyTasks = new ArrayList<>();
            }
        }

        // Weekly Tasks (기존대로)
        String weeklyJson = prefs.getString("weekly_tasks", null);
        if (weeklyJson != null) {
            Type type = new TypeToken<List<WeeklyTask>>(){}.getType();
            weeklyTasks = gson.fromJson(weeklyJson, type);
        } else {
            weeklyTasks = new ArrayList<>();
        }
    }

    // ★ dailyTasks 저장 시 오늘 날짜도 같이 저장
    private void saveTasksToStorage() {
        Gson gson = new Gson();

        String dailyJson = gson.toJson(dailyTasks);
        prefs.edit().putString("daily_tasks", dailyJson).apply();

        saveLastDailyDate(LocalDate.now()); // 오늘 날짜를 같이 저장!

        String weeklyJson = gson.toJson(weeklyTasks);
        prefs.edit().putString("weekly_tasks", weeklyJson).apply();
    }

    // ★ 날짜만 따로 저장하는 함수
    private void saveLastDailyDate(LocalDate date) {
        prefs.edit().putString("last_daily_date", date.toString()).apply();
    }

    private void showAddDailyDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_daily, null);
        EditText priorityInput = dialogView.findViewById(R.id.input_priority);
        EditText taskInput = dialogView.findViewById(R.id.input_task);
        Spinner durationSpinner = dialogView.findViewById(R.id.spinner_duration);
        EditText customDurationInput = dialogView.findViewById(R.id.custom_duration_input);

        String[] options = {"10분", "30분", "1시간", "1시간 30분", "2시간", "직접 입력"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);

        durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                customDurationInput.setVisibility(options[position].equals("직접 입력") ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        new AlertDialog.Builder(getContext())
                .setTitle("할 일 추가")
                .setView(dialogView)
                .setPositiveButton("추가", (dialog, which) -> {
                    String taskText = taskInput.getText().toString();
                    String timeText = options[durationSpinner.getSelectedItemPosition()].equals("직접 입력")
                            ? customDurationInput.getText().toString()
                            : options[durationSpinner.getSelectedItemPosition()];
                    int priority = 0;
                    try {
                        priority = Integer.parseInt(priorityInput.getText().toString());
                    } catch (NumberFormatException ignored) {}

                    dailyTasks.removeIf(task -> task.getTask().equals("할 일을 등록하세요"));
                    dailyTasks.add(0, new DailyTask(priority, taskText, timeText, false, false, LocalDate.now()));
                    dailyAdapter.notifyDataSetChanged();
                    saveTasksToStorage();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void setupDailyList() {
        dailyAdapter = new DailyTaskAdapter(dailyTasks, new DailyTaskAdapter.OnTaskActionListener() {
            @Override
            public void onEdit(int position) {
                showEditDailyDialog(position);
            }
            @Override
            public void onDelete(int position) {
                // 실제 삭제 동작이 필요하면 여기에 구현!
                // 예: dailyTasks.remove(position); dailyAdapter.notifyDataSetChanged(); saveTasksToStorage();
            }
        });
        dailyAdapter.setOnTaskChangedListener(this::saveTasksToStorage);
        dailyRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        dailyRecyclerView.setAdapter(dailyAdapter);

        if (dailyTasks.isEmpty()) {
            dailyTasks.add(new DailyTask(0, "할 일을 등록하세요", "", false, false, LocalDate.now()));
            dailyAdapter.notifyDataSetChanged();
        }
    }


    private void setupWeeklyList() {
        weeklyAdapter = new WeeklyTaskAdapter(
                weeklyTasks,
                new WeeklyTaskAdapter.OnTaskClickListener() {
                    @Override
                    public void onTaskClick(int position) {
                        showEditWeeklyDialog(position);
                    }
                },
                new WeeklyTaskAdapter.OnTaskDeleteListener() {
                    @Override
                    public void onDeleteClick(int position) {
                        weeklyTasks.remove(position);
                        weeklyAdapter.notifyDataSetChanged();
                        saveTasksToStorage();
                    }
                }
        );
        weeklyAdapter.setOnTaskChangedListener(this::saveTasksToStorage);
        weeklyRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        weeklyRecyclerView.setAdapter(weeklyAdapter);

        if (weeklyTasks.isEmpty()) {
            weeklyTasks.add(new WeeklyTask("할 일을 등록하세요", "", false));
            weeklyAdapter.notifyDataSetChanged();
        }
    }

    private void showDdayPicker() {
        LocalDate today = LocalDate.now();
        DatePickerDialog picker = new DatePickerDialog(getContext(), (view, y, m, d) -> {
            LocalDate selected = LocalDate.of(y, m + 1, d);
            prefs.edit().putString("dday_target", selected.toString()).apply();
            updateTodayAndDdayText();
        }, today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth());
        picker.show();
    }

    private void showEditDailyDialog(int pos) {
        DailyTask task = dailyTasks.get(pos);
        if (task.getTask().equals("할 일을 등록하세요")) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_daily, null);
        EditText priorityInput = dialogView.findViewById(R.id.input_priority);
        EditText taskInput = dialogView.findViewById(R.id.input_task);
        Spinner durationSpinner = dialogView.findViewById(R.id.spinner_duration);
        EditText customDurationInput = dialogView.findViewById(R.id.custom_duration_input);

        String[] options = {"10분", "30분", "1시간", "1시간 30분", "2시간", "직접 입력"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);

        priorityInput.setText(String.valueOf(task.getPriority()));
        taskInput.setText(task.getTask());
        customDurationInput.setVisibility(View.GONE);

        durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                customDurationInput.setVisibility(options[position].equals("직접 입력") ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        new AlertDialog.Builder(getContext())
                .setTitle("할 일 수정 또는 삭제")
                .setView(dialogView)
                .setPositiveButton("수정", (dialog, which) -> {
                    String title = taskInput.getText().toString().trim();
                    String duration = durationSpinner.getSelectedItem().toString();
                    if (duration.equals("직접 입력")) {
                        duration = customDurationInput.getText().toString().trim();
                    }
                    task.setTask(title);
                    task.setEstimatedTime(duration);
                    task.setPriority(Integer.parseInt(priorityInput.getText().toString()));
                    dailyAdapter.notifyItemChanged(pos);
                    saveTasksToStorage();
                })
                .setNegativeButton("삭제", (dialog, which) -> {
                    dailyTasks.remove(pos);
                    if (dailyTasks.isEmpty()) {
                        dailyTasks.add(new DailyTask(0, "할 일을 등록하세요", "", false, false, LocalDate.now()));
                    }
                    dailyAdapter.notifyDataSetChanged();
                    saveTasksToStorage();
                })
                .show();
    }

    private void showAddWeeklyDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_weekly, null);
        EditText taskInput = dialogView.findViewById(R.id.input_task);
        TextView deadlineView = dialogView.findViewById(R.id.txt_deadline);
        Button pickButton = dialogView.findViewById(R.id.btn_deadline_select);

        final String[] deadline = {""};
        pickButton.setOnClickListener(v -> {
            LocalDate today = LocalDate.now();
            DatePickerDialog picker = new DatePickerDialog(getContext(), (view, y, m, d) -> {
                LocalDate selected = LocalDate.of(y, m + 1, d);
                deadline[0] = selected.toString();
                deadlineView.setText(deadline[0]);
            }, today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth());
            picker.show();
        });

        new AlertDialog.Builder(getContext())
                .setTitle("일정 추가")
                .setView(dialogView)
                .setPositiveButton("추가", (d, w) -> {
                    String taskText = taskInput.getText().toString();
                    if (!weeklyTasks.isEmpty() && weeklyTasks.get(0).getTask().equals("할 일을 등록하세요")) {
                        weeklyTasks.remove(0);
                    }
                    weeklyTasks.add(0, new WeeklyTask(taskText, deadline[0], false));
                    weeklyAdapter.notifyDataSetChanged();
                    saveTasksToStorage();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showEditWeeklyDialog(int pos) {
        WeeklyTask task = weeklyTasks.get(pos);
        if (task.getTask().equals("할 일을 등록하세요")) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_weekly, null);
        EditText taskInput = dialogView.findViewById(R.id.input_task);
        TextView deadlineView = dialogView.findViewById(R.id.txt_deadline);
        Button pickButton = dialogView.findViewById(R.id.btn_deadline_select);
        final String[] deadline = {task.getDeadline()};

        taskInput.setText(task.getTask());
        deadlineView.setText(task.getDeadline());

        pickButton.setOnClickListener(v -> {
            LocalDate today = LocalDate.now();
            DatePickerDialog picker = new DatePickerDialog(getContext(), (view, y, m, d) -> {
                LocalDate selected = LocalDate.of(y, m + 1, d);
                deadline[0] = selected.toString();
                deadlineView.setText(deadline[0]);
            }, today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth());
            picker.show();
        });

        new AlertDialog.Builder(getContext())
                .setTitle("일정 수정 또는 삭제")
                .setView(dialogView)
                .setPositiveButton("수정", (dialog, which) -> {
                    task.setTask(taskInput.getText().toString());
                    task.setDeadline(deadline[0]);
                    weeklyAdapter.notifyItemChanged(pos);
                    saveTasksToStorage();
                })
                .setNegativeButton("삭제", (dialog, which) -> {
                    weeklyTasks.remove(pos);
                    if (weeklyTasks.isEmpty()) {
                        weeklyTasks.add(new WeeklyTask("할 일을 등록하세요", "", false));
                    }
                    weeklyAdapter.notifyDataSetChanged();
                    saveTasksToStorage();
                })
                .show();
    }


}
