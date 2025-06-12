package com.example.scheduleapp;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DDay {
    private String id;
    private String title;
    private Date targetDate;
    private int daysLeft;

    public DDay() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.title = "";
        this.targetDate = new Date();
        calculateDaysLeft();
    }

    public DDay(String title, Date targetDate) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.title = title;
        this.targetDate = targetDate;
        calculateDaysLeft();
    }

    public void calculateDaysLeft() {
        Date today = new Date();
        long diffInMillies = targetDate.getTime() - today.getTime();
        this.daysLeft = (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public String getDDayString() {
        if (daysLeft > 0) {
            return "D-" + daysLeft;
        } else if (daysLeft == 0) {
            return "D-Day";
        } else {
            return "D+" + Math.abs(daysLeft);
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Date getTargetDate() { return targetDate; }
    public void setTargetDate(Date targetDate) {
        this.targetDate = targetDate;
        calculateDaysLeft();
    }

    public int getDaysLeft() { return daysLeft; }
}