package com.example.scheduleapp;

import java.time.LocalDate;

public class DailyTask {
    private int priority;
    private String task;
    private String estimatedTime;
    private boolean completed;
    private boolean fixed;
    private LocalDate date;

    public DailyTask(int priority, String task, String estimatedTime, boolean completed, boolean fixed, LocalDate date) {
        this.priority = priority;
        this.task = task;
        this.estimatedTime = estimatedTime;
        this.completed = completed;
        this.fixed = fixed;
        this.date = date;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public int getEstimatedMinutes() {
        if (estimatedTime == null || estimatedTime.isEmpty()) return Integer.MAX_VALUE;

        try {
            int minutes = 0;
            if (estimatedTime.contains("시간")) {
                String[] parts = estimatedTime.split("시간");
                minutes += Integer.parseInt(parts[0].trim()) * 60;
                if (parts.length > 1 && parts[1].contains("분")) {
                    minutes += Integer.parseInt(parts[1].replace("분", "").trim());
                }
            } else if (estimatedTime.contains("분")) {
                minutes += Integer.parseInt(estimatedTime.replace("분", "").trim());
            }
            return minutes;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }
}
