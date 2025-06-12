package com.example.scheduleapp;

import java.io.Serializable;

public class Schedule implements Serializable {
    private String title;
    private int startHour;
    private int startMinute;
    private int blockLength;
    private String color;
    private boolean isFixed;

    // 기본 생성자
    public Schedule() {}

    public Schedule(String title, int startHour, int startMinute, int blockLength, String color) {
        this.title = title;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.blockLength = blockLength;
        this.color = color;
        this.isFixed = false;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getStartHour() { return startHour; }
    public void setStartHour(int startHour) { this.startHour = startHour; }

    public int getStartMinute() { return startMinute; }
    public void setStartMinute(int startMinute) { this.startMinute = startMinute; }

    public int getBlockLength() { return blockLength; }
    public void setBlockLength(int blockLength) { this.blockLength = blockLength; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isFixed() { return isFixed; }
    public void setFixed(boolean fixed) { isFixed = fixed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return startHour == schedule.startHour &&
                startMinute == schedule.startMinute &&
                blockLength == schedule.blockLength &&
                isFixed == schedule.isFixed &&
                java.util.Objects.equals(title, schedule.title) &&
                java.util.Objects.equals(color, schedule.color);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(title, startHour, startMinute, blockLength, color, isFixed);
    }
}
