package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.BacSi;
import java.util.List;

public class BacSiAdapter extends RecyclerView.Adapter<BacSiAdapter.BacSiViewHolder> {

    private List<BacSi> bacSiList;
    private OnBacSiClickListener listener;

    public interface OnBacSiClickListener {
        void onBacSiClick(BacSi bacSi);
    }

    public BacSiAdapter(List<BacSi> bacSiList, OnBacSiClickListener listener) {
        this.bacSiList = bacSiList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BacSiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bac_si, parent, false);
        return new BacSiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BacSiViewHolder holder, int position) {
        BacSi bacSi = bacSiList.get(position);
        
        // Hiển thị tên
        String hoTen = bacSi.getHoTen() != null ? bacSi.getHoTen() : "Bác sĩ";
        holder.tvHoTen.setText(hoTen);
        
        // Hiển thị kinh nghiệm
        int namKinhNghiem = bacSi.getNamKinhNghiem();
        holder.tvKinhNghiem.setText(namKinhNghiem > 0 ? namKinhNghiem + " năm kinh nghiệm" : "");
        
        // Hiển thị chuyên khoa
        String chuyenKhoa = bacSi.getChuyenKhoa();
        holder.tvChuyenKhoa.setText(chuyenKhoa != null ? chuyenKhoa : "");
        
        // Hiển thị địa chỉ
        String diaChi = bacSi.getDiaChi();
        holder.tvDiaChi.setText(diaChi != null ? diaChi : "");
        
        // Click listener
        holder.btnDatLich.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBacSiClick(bacSi);
            }
        });
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBacSiClick(bacSi);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bacSiList.size();
    }

    public void updateList(List<BacSi> newList) {
        this.bacSiList = newList;
        notifyDataSetChanged();
    }

    static class BacSiViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvHoTen, tvKinhNghiem, tvChuyenKhoa, tvDiaChi;
        Button btnDatLich;

        public BacSiViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvHoTen = itemView.findViewById(R.id.tvHoTen);
            tvKinhNghiem = itemView.findViewById(R.id.tvKinhNghiem);
            tvChuyenKhoa = itemView.findViewById(R.id.tvChuyenKhoa);
            tvDiaChi = itemView.findViewById(R.id.tvDiaChi);
            btnDatLich = itemView.findViewById(R.id.btnDatLich);
        }
    }
}
