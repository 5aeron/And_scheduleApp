package com.example.scheduleapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<CalendarDay> calendarDays;
    private LocalDate selectedDate;
    private OnDayClickListener onDayClickListener;

    public interface OnDayClickListener {
        void onDayClick(LocalDate date);
    }

    public CalendarAdapter(List<CalendarDay> calendarDays) {
        this.calendarDays = calendarDays;
        this.selectedDate = null;
    }

    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.onDayClickListener = listener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarDay calendarDay = calendarDays.get(position);
        LocalDate date = calendarDay.getDate();

        // 날짜 출력
        holder.dayText.setText(String.valueOf(date.getDayOfMonth()));

        if (!calendarDay.isCurrentMonth()) {
            // 현재 달이 아닌 경우: 회색 + 클릭 비활성화
            holder.dayText.setTextColor(Color.LTGRAY);
            holder.itemView.setClickable(false);
            holder.dayText.setBackgroundColor(Color.TRANSPARENT);
            holder.eventDot.setVisibility(View.GONE);
        } else {
            // 현재 달
            holder.itemView.setClickable(true);

            // 선택된 날짜 강조
            if (selectedDate != null && date.equals(selectedDate)) {
                holder.dayText.setBackgroundColor(Color.RED);
                holder.dayText.setTextColor(Color.WHITE);
            } else {
                holder.dayText.setBackgroundColor(Color.TRANSPARENT);
                holder.dayText.setTextColor(Color.BLACK);
            }

            // 이벤트 점 표시
            holder.eventDot.setVisibility(calendarDay.hasEvent() ? View.VISIBLE : View.GONE);

            // 클릭 이벤트
            holder.itemView.setOnClickListener(v -> {
                if (onDayClickListener != null) {
                    onDayClickListener.onDayClick(date);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return calendarDays.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView dayText;
        View eventDot;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.day_text);
            eventDot = itemView.findViewById(R.id.event_dot);
        }
    }
}
