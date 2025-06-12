package com.example.scheduleapp;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;

public class CalendarDay {
    private LocalDate date;
    private boolean isCurrentMonth;
    private boolean hasEvent;

    public CalendarDay(LocalDate date, boolean isCurrentMonth) {
        this.date = date;
        this.isCurrentMonth = isCurrentMonth;
        this.hasEvent = false;
    }

    public LocalDate getDate() {
        return date;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int getDay() {
        return date.getDayOfMonth();
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public boolean hasEvent() {
        return hasEvent;
    }

    public void setHasEvent(boolean hasEvent) {
        this.hasEvent = hasEvent;
    }
}
