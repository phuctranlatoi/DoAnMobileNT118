package com.example.doannt118.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.TimeSlot;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
    
    private List<TimeSlot> timeSlotList;
    private OnTimeSlotClickListener listener;
    private int selectedPosition = -1;

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(TimeSlot timeSlot, int position);
    }

    public TimeSlotAdapter(List<TimeSlot> timeSlotList, OnTimeSlotClickListener listener) {
        this.timeSlotList = timeSlotList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlot timeSlot = timeSlotList.get(position);
        
        holder.tvKhungGio.setText(timeSlot.getKhungGio());
        
        // QUAN TRỌNG: Các slot đã đặt không nên xuất hiện trong timeSlotList
        // Nhưng để đảm bảo an toàn, vẫn kiểm tra và ẩn nếu cần
        if (timeSlot.isBooked()) {
            // Slot đã đặt - ẩn hoàn toàn
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        }
        
        // Slot còn trống - hiển thị bình thường
        holder.itemView.setVisibility(View.VISIBLE);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 8, 8, 8); // Thêm margin cho đẹp
        holder.itemView.setLayoutParams(params);
        
        // Highlight slot được chọn
        if (position == selectedPosition) {
            holder.cardTimeSlot.setCardBackgroundColor(
                holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
            holder.tvKhungGio.setTextColor(
                holder.itemView.getContext().getResources().getColor(R.color.white));
        } else {
            holder.cardTimeSlot.setCardBackgroundColor(
                holder.itemView.getContext().getResources().getColor(R.color.white));
            holder.tvKhungGio.setTextColor(
                holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
        }
        
        holder.itemView.setOnClickListener(v -> {
            // Kiểm tra lại trước khi cho phép chọn
            if (timeSlot.isBooked()) {
                return; // Không cho phép chọn slot đã đặt
            }
            
            int oldPosition = selectedPosition;
            selectedPosition = position;
            
            // Refresh old and new selected items
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition);
            }
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onTimeSlotClick(timeSlot, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeSlotList.size();
    }

    public void updateTimeSlots(List<TimeSlot> newTimeSlots) {
        Log.d("TimeSlotAdapter", "Updating time slots. Old count: " + this.timeSlotList.size() + ", New count: " + newTimeSlots.size());
        
        // Log các slot để debug
        for (int i = 0; i < newTimeSlots.size(); i++) {
            TimeSlot slot = newTimeSlots.get(i);
            Log.d("TimeSlotAdapter", "Slot " + i + ": " + slot.getKhungGio() + " - Booked: " + slot.isBooked());
        }
        
        this.timeSlotList = newTimeSlots;
        this.selectedPosition = -1; // Reset selection
        notifyDataSetChanged();
    }

    public TimeSlot getSelectedTimeSlot() {
        if (selectedPosition >= 0 && selectedPosition < timeSlotList.size()) {
            return timeSlotList.get(selectedPosition);
        }
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardTimeSlot;
        TextView tvKhungGio;

        ViewHolder(View itemView) {
            super(itemView);
            cardTimeSlot = itemView.findViewById(R.id.cardTimeSlot);
            tvKhungGio = itemView.findViewById(R.id.tvKhungGio);
        }
    }
}