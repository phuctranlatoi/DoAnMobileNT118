package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.TinNhanBacSi;
import de.hdodenhof.circleimageview.CircleImageView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TinNhanBacSiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_BENH_NHAN = 1;
    private static final int TYPE_BAC_SI = 2;
    
    private List<TinNhanBacSi> danhSachTinNhan;
    private SimpleDateFormat timeFormat;
    
    public TinNhanBacSiAdapter() {
        this.danhSachTinNhan = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @Override
    public int getItemViewType(int position) {
        TinNhanBacSi tinNhan = danhSachTinNhan.get(position);
        return tinNhan.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BENH_NHAN ? 
               TYPE_BENH_NHAN : TYPE_BAC_SI;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        if (viewType == TYPE_BENH_NHAN) {
            View view = inflater.inflate(R.layout.item_tin_nhan_benh_nhan, parent, false);
            return new BenhNhanViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_tin_nhan_bac_si, parent, false);
            return new BacSiViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TinNhanBacSi tinNhan = danhSachTinNhan.get(position);
        
        if (holder instanceof BenhNhanViewHolder) {
            ((BenhNhanViewHolder) holder).bind(tinNhan);
        } else if (holder instanceof BacSiViewHolder) {
            ((BacSiViewHolder) holder).bind(tinNhan);
        }
    }
    
    @Override
    public int getItemCount() {
        return danhSachTinNhan.size();
    }
    
    public void setData(List<TinNhanBacSi> danhSachTinNhan) {
        // Clear data cũ trước khi set data mới
        this.danhSachTinNhan.clear();
        if (danhSachTinNhan != null) {
            this.danhSachTinNhan.addAll(danhSachTinNhan);
        }
        notifyDataSetChanged();
    }
    
    public void themTinNhan(TinNhanBacSi tinNhan) {
        danhSachTinNhan.add(tinNhan);
        notifyItemInserted(danhSachTinNhan.size() - 1);
    }
    
    // ViewHolder cho tin nhắn của bệnh nhân
    class BenhNhanViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNoiDung, tvThoiGian;
        
        public BenhNhanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoiDung = itemView.findViewById(R.id.tvNoiDung);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
        }
        
        public void bind(TinNhanBacSi tinNhan) {
            tvNoiDung.setText(tinNhan.getNoiDung());
            if (tinNhan.getThoiGianGui() != null) {
                tvThoiGian.setText(timeFormat.format(tinNhan.getThoiGianGui().toDate()));
            }
        }
    }
    
    // ViewHolder cho tin nhắn của bác sĩ
    class BacSiViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNoiDung, tvThoiGian;
        private CircleImageView ivAvatar;
        
        public BacSiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoiDung = itemView.findViewById(R.id.tvNoiDung);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
        
        public void bind(TinNhanBacSi tinNhan) {
            tvNoiDung.setText(tinNhan.getNoiDung());
            if (tinNhan.getThoiGianGui() != null) {
                tvThoiGian.setText(timeFormat.format(tinNhan.getThoiGianGui().toDate()));
            }
            // Có thể load avatar từ URL nếu có
            ivAvatar.setImageResource(R.drawable.ic_doctor);
        }
    }
}