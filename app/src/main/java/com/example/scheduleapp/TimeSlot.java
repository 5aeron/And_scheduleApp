package com.example.scheduleapp;

public class TimeSlot {
    private int hour;
    private int minute;
    private Schedule schedule;

    public TimeSlot(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        this.schedule = null;
    }

    public int getHour() { return hour; }
    public int getMinute() { return minute; }
    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }
}
