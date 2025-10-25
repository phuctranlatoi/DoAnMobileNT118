package com.example.doannt118.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.BenhAn;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BenhAnAdapter extends RecyclerView.Adapter<BenhAnAdapter.ViewHolder> {

    private List<BenhAn> medicalRecords;
    private String maTaiKhoan;

    public BenhAnAdapter(List<BenhAn> medicalRecords, String maTaiKhoan) {
        this.medicalRecords = medicalRecords;
        this.maTaiKhoan = maTaiKhoan;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_benhan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BenhAn benhAn = medicalRecords.get(position);
        holder.tvMaBenhAn.setText("Mã Bệnh Án: " + benhAn.getMaBenhAn());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvNgayKham.setText("Ngày Khám: " + (benhAn.getNgayKham() != null ? dateFormat.format(benhAn.getNgayKham().toDate()) : "N/A"));
        holder.tvChanDoan.setText("Chẩn Đoán: " + (benhAn.getChanDoan() != null ? benhAn.getChanDoan() : "N/A"));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ChiTietBenhAnActivity.class);
            intent.putExtra("MA_BENH_AN", benhAn.getMaBenhAn());
            intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return medicalRecords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaBenhAn, tvNgayKham, tvChanDoan;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaBenhAn = itemView.findViewById(R.id.tvMaBenhAn);
            tvNgayKham = itemView.findViewById(R.id.tvNgayKham);
            tvChanDoan = itemView.findViewById(R.id.tvChanDoan);
        }
    }
}