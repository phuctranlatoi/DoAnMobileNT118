package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.BacSi;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class BacSiChatAdapter extends RecyclerView.Adapter<BacSiChatAdapter.ViewHolder> {
    
    private List<BacSi> danhSachBacSi;
    private OnBacSiClickListener listener;
    
    public interface OnBacSiClickListener {
        void onBacSiClick(BacSi bacSi);
    }
    
    public BacSiChatAdapter(OnBacSiClickListener listener) {
        this.danhSachBacSi = new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bac_si_chat, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BacSi bacSi = danhSachBacSi.get(position);
        holder.bind(bacSi);
    }
    
    @Override
    public int getItemCount() {
        return danhSachBacSi.size();
    }
    
    public void setData(List<BacSi> danhSachBacSi) {
        this.danhSachBacSi = danhSachBacSi;
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivAvatar;
        private TextView tvTenBacSi, tvChuyenKhoa, tvTrangThai;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvTenBacSi = itemView.findViewById(R.id.tvTenBacSi);
            tvChuyenKhoa = itemView.findViewById(R.id.tvChuyenKhoa);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onBacSiClick(danhSachBacSi.get(position));
                    }
                }
            });
        }
        
        public void bind(BacSi bacSi) {
            tvTenBacSi.setText("BS. " + bacSi.getHoTen());
            
            // Hiển thị chuyên khoa (có thể lấy từ bangCap hoặc hocVi)
            String chuyenKhoa = bacSi.getBangCap();
            if (chuyenKhoa == null || chuyenKhoa.isEmpty()) {
                chuyenKhoa = bacSi.getHocVi();
            }
            if (chuyenKhoa == null || chuyenKhoa.isEmpty()) {
                chuyenKhoa = "Bác sĩ đa khoa";
            }
            tvChuyenKhoa.setText(chuyenKhoa);
            
            // Hiển thị trạng thái
            tvTrangThai.setText("Sẵn sàng tư vấn");
            
            // Load avatar (có thể dùng Glide nếu có URL)
            ivAvatar.setImageResource(R.drawable.ic_doctor);
        }
    }
}