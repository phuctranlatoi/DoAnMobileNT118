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

public class ThuocCanUongAdapter extends RecyclerView.Adapter<ThuocCanUongAdapter.ViewHolder> {
    private Context context;
    private List<ChiTietDonThuoc> thuocList;

    public ThuocCanUongAdapter(Context context) {
        this.context = context;
        this.thuocList = new ArrayList<>();
    }

    public void setData(List<ChiTietDonThuoc> list) {
        this.thuocList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<ChiTietDonThuoc> getData() {
        return thuocList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_thuoc_can_uong, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChiTietDonThuoc thuoc = thuocList.get(position);
        
        holder.tvTenThuoc.setText(thuoc.getTenThuoc() != null ? thuoc.getTenThuoc() : "Thuốc");
        // Dùng getLieuDungDayDu() để tự động tạo nếu null
        String lieuDung = thuoc.getLieuDungDayDu();
        holder.tvLieuDung.setText(lieuDung != null ? lieuDung : "Theo chỉ định");
        holder.tvSoLuong.setText("Số lượng: " + thuoc.getSoLuong() + " viên");
    }

    @Override
    public int getItemCount() {
        return thuocList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenThuoc, tvLieuDung, tvSoLuong;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenThuoc = itemView.findViewById(R.id.tvTenThuoc);
            tvLieuDung = itemView.findViewById(R.id.tvLieuDung);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
        }
    }
}
