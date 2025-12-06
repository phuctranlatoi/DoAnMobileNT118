package com.example.doannt118.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.ChiTietDonThuoc;
import java.util.ArrayList;
import java.util.List;

public class ThuocKeDonAdapter extends RecyclerView.Adapter<ThuocKeDonAdapter.ViewHolder> {
    private Context context;
    private List<ChiTietDonThuoc> thuocList;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public ThuocKeDonAdapter(Context context, List<ChiTietDonThuoc> thuocList, 
                            OnDeleteClickListener deleteListener) {
        this.context = context;
        this.thuocList = thuocList != null ? thuocList : new ArrayList<>();
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_thuoc_ke_don, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChiTietDonThuoc thuoc = thuocList.get(position);
        
        holder.tvTenThuoc.setText(thuoc.getTenThuoc());
        holder.tvSoLuong.setText("📦 Tổng: " + thuoc.getSoLuong() + " viên");
        
        // Hiển thị liều dùng đầy đủ
        String lieuDungDayDu = thuoc.getLieuDungDayDu();
        if (lieuDungDayDu != null && !lieuDungDayDu.isEmpty()) {
            holder.tvLieuDung.setText("💊 " + lieuDungDayDu);
        } else {
            holder.tvLieuDung.setText("💊 " + thuoc.getLieuDung());
        }
        
        // Hiển thị cách dùng nếu có
        if (thuoc.getCachDung() != null && !thuoc.getCachDung().isEmpty()) {
            holder.tvCachDung.setVisibility(View.VISIBLE);
            holder.tvCachDung.setText("Uống " + thuoc.getCachDung());
        } else {
            holder.tvCachDung.setVisibility(View.GONE);
        }
        
        holder.ivDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return thuocList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenThuoc, tvLieuDung, tvSoLuong, tvCachDung;
        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenThuoc = itemView.findViewById(R.id.tvTenThuoc);
            tvLieuDung = itemView.findViewById(R.id.tvLieuDung);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            tvCachDung = itemView.findViewById(R.id.tvCachDung);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
