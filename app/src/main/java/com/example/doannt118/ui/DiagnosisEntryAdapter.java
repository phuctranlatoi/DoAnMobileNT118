package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;

import java.util.List;

public class DiagnosisEntryAdapter extends RecyclerView.Adapter<DiagnosisEntryAdapter.ViewHolder> {

    private List<DiagnosisEntry> diagnosisList;

    public DiagnosisEntryAdapter(List<DiagnosisEntry> diagnosisList) {
        this.diagnosisList = diagnosisList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diagnosis_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiagnosisEntry entry = diagnosisList.get(position);
        holder.tvNgay.setText(entry.getNgay());
        holder.tvChanDoan.setText(entry.getChanDoan());
    }

    @Override
    public int getItemCount() {
        return diagnosisList.size();
    }

    public static class DiagnosisEntry {
        private final String ngay;
        private final String chanDoan;

        public DiagnosisEntry(String ngay, String chanDoan) {
            this.ngay = ngay;
            this.chanDoan = chanDoan;
        }

        public String getNgay() {
            return ngay;
        }

        public String getChanDoan() {
            return chanDoan;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNgay, tvChanDoan;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNgay = itemView.findViewById(R.id.tvNgay);
            tvChanDoan = itemView.findViewById(R.id.tvChanDoan);
        }
    }
}