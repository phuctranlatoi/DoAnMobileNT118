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
import com.example.doannt118.model.DonThuoc;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DonThuocAdapter extends RecyclerView.Adapter<DonThuocAdapter.ViewHolder> {

    private Context context;
    private List<DonThuoc> donThuocList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DonThuoc donThuoc);
    }

    public DonThuocAdapter(Context context, List<DonThuoc> donThuocList, OnItemClickListener listener) {
        this.context = context;
        this.donThuocList = donThuocList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_don_thuoc, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonThuoc donThuoc = donThuocList.get(position);
        
        holder.tvMaDonThuoc.setText("Mã đơn: " + donThuoc.getMaDonThuoc());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (donThuoc.getNgayLap() != null) {
            holder.tvNgayLap.setText("Ngày lập: " + sdf.format(donThuoc.getNgayLap()));
        } else if (donThuoc.getNgayKeDon() != null) {
            holder.tvNgayLap.setText("Ngày lập: " + sdf.format(donThuoc.getNgayKeDon().toDate()));
        } else {
            holder.tvNgayLap.setText("Ngày lập: Không rõ");
        }
        
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(donThuoc);
            }
        });
    }

    @Override
    public int getItemCount() {
        return donThuocList.size();
    }
    
    public void updateData(List<DonThuoc> newList) {
        this.donThuocList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvMaDonThuoc, tvNgayLap;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvMaDonThuoc = itemView.findViewById(R.id.tvMaDonThuoc);
            tvNgayLap = itemView.findViewById(R.id.tvNgayLap);
        }
    }
}
