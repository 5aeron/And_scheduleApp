package com.example.scheduleapp;

public class Schedule {
    private String title;
    private int hour;
    private int minute;
    private int durationBlocks;
    private String color;
    private boolean fixed;  // 고정 일정 여부

    // 일반 생성자
    public Schedule(String title, int hour, int minute, int durationBlocks, String color) {
        this(title, hour, minute, durationBlocks, color, false); // 기본값: 고정 아님
    }

    // 고정 여부 포함 생성자
    public Schedule(String title, int hour, int minute, int durationBlocks, String color, boolean fixed) {
        this.title = title;
        this.hour = hour;
        this.minute = minute;
        this.durationBlocks = durationBlocks;
        this.color = color;
        this.fixed = fixed;
    }

    // Getter
    public String getTitle() { return title; }
    public int getHour() { return hour; }
    public int getMinute() { return minute; }
    public int getDurationBlocks() { return durationBlocks; }
    public String getColor() { return color; }
    public boolean isFixed() { return fixed; }

    // Setter (필요 시 사용)
    public void setTitle(String title) { this.title = title; }
    public void setHour(int hour) { this.hour = hour; }
    public void setMinute(int minute) { this.minute = minute; }
    public void setDurationBlocks(int durationBlocks) { this.durationBlocks = durationBlocks; }
    public void setColor(String color) { this.color = color; }
    public void setFixed(boolean fixed) { this.fixed = fixed; }
}
