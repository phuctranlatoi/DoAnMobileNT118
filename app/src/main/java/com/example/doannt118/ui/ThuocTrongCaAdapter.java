package com.example.doannt118.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.ChiTietDonThuoc;
import java.util.ArrayList;
import java.util.List;

public class ThuocTrongCaAdapter extends RecyclerView.Adapter<ThuocTrongCaAdapter.ViewHolder> {
    
    private Context context;
    private List<ChiTietDonThuoc> thuocList;

    public ThuocTrongCaAdapter(Context context) {
        this.context = context;
        this.thuocList = new ArrayList<>();
    }
    
    public void setData(List<ChiTietDonThuoc> list) {
        this.thuocList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_thuoc_trong_ca, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChiTietDonThuoc thuoc = thuocList.get(position);
        
        holder.tvTenThuoc.setText(thuoc.getTenThuoc());
        
        // Hiển thị liều dùng
        String lieuDung = thuoc.getLieuDungDayDu();
        if (lieuDung != null && !lieuDung.isEmpty()) {
            holder.tvLieuDung.setText(lieuDung);
            holder.tvLieuDung.setVisibility(View.VISIBLE);
        } else {
            holder.tvLieuDung.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return thuocList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenThuoc, tvLieuDung;

        ViewHolder(View itemView) {
            super(itemView);
            tvTenThuoc = itemView.findViewById(R.id.tvTenThuoc);
            tvLieuDung = itemView.findViewById(R.id.tvLieuDung);
        }
    }
}