package com.example.doannt118.ui;

import android.content.Context;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BenhAnAdapter extends RecyclerView.Adapter<BenhAnAdapter.ViewHolder> {
    private Context context;
    private List<BenhAn> benhAnList;
    private SimpleDateFormat dateFormat;

    public BenhAnAdapter(Context context) {
        this.context = context;
        this.benhAnList = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    public void setData(List<BenhAn> list) {
        this.benhAnList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_benh_an, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BenhAn benhAn = benhAnList.get(position);
        
        holder.tvMaBenhAn.setText(benhAn.getMaBenhAn());
        
        if (benhAn.getNgayKham() != null) {
            holder.tvNgayKham.setText(dateFormat.format(benhAn.getNgayKham().toDate()));
        }
        
        holder.tvChanDoan.setText("Chẩn đoán: " + (benhAn.getChanDoan() != null ? benhAn.getChanDoan() : "Chưa có"));
        holder.tvBacSi.setText("Mã bác sĩ: " + (benhAn.getMaBacSi() != null ? benhAn.getMaBacSi() : "N/A"));
        
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChiTietBenhAnActivity.class);
            intent.putExtra("maBenhAn", benhAn.getMaBenhAn());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return benhAnList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaBenhAn, tvNgayKham, tvChanDoan, tvBacSi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaBenhAn = itemView.findViewById(R.id.tvMaBenhAn);
            tvNgayKham = itemView.findViewById(R.id.tvNgayKham);
            tvChanDoan = itemView.findViewById(R.id.tvChanDoan);
            tvBacSi = itemView.findViewById(R.id.tvBacSi);
        }
    }
}
