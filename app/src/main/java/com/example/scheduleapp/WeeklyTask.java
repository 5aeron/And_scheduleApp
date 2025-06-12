package com.example.scheduleapp;

public class WeeklyTask {
    private String task;
    private String deadline;
    private boolean completed;

    public WeeklyTask(String task, String deadline, boolean completed) {
        this.task = task;
        this.deadline = deadline;
        this.completed = completed;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
