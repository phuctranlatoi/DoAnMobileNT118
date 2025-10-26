package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.utils.Dateutils;

import java.util.List;

public class ActivityHistoryAdapter extends RecyclerView.Adapter<ActivityHistoryAdapter.ViewHolder> {
    private List<LichSuHoatDong> list;

    public ActivityHistoryAdapter(List<LichSuHoatDong> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LichSuHoatDong lichSu = list.get(position);
        holder.tvHanhDong.setText(lichSu.getHanhDong());
        holder.tvThoiGian.setText(Dateutils.format(lichSu.getThoiGian()));
        holder.tvChiTiet.setText(lichSu.getChiTiet() != null ? lichSu.getChiTiet() : "N/A");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHanhDong, tvThoiGian, tvChiTiet;

        ViewHolder(View itemView) {
            super(itemView);
            tvHanhDong = itemView.findViewById(R.id.tvHanhDong);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
            tvChiTiet = itemView.findViewById(R.id.tvChiTiet);
        }
    }
}