package com.example.doannt118.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.HoaDon;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HoaDonAdapter extends RecyclerView.Adapter<HoaDonAdapter.ViewHolder> {

    private Context context;
    private List<HoaDon> hoaDonList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HoaDon hoaDon);
    }

    public HoaDonAdapter(Context context, List<HoaDon> hoaDonList, OnItemClickListener listener) {
        this.context = context;
        this.hoaDonList = hoaDonList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hoa_don, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HoaDon hoaDon = hoaDonList.get(position);
        
        holder.tvMaHoaDon.setText("Mã hóa đơn: " + hoaDon.getMaHoaDon());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvNgayLap.setText("Ngày lập: " + sdf.format(hoaDon.getNgayLap()));
        
        holder.tvTongTien.setText(String.format("Tổng tiền: %,.0f đ", hoaDon.getTongTien()));
        
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(hoaDon);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hoaDonList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvMaHoaDon, tvNgayLap, tvTongTien;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvMaHoaDon = itemView.findViewById(R.id.tvMaHoaDon);
            tvNgayLap = itemView.findViewById(R.id.tvNgayLap);
            tvTongTien = itemView.findViewById(R.id.tvTongTien);
        }
    }
}
