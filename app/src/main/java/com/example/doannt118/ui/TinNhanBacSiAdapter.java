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
    
    private static final int TYPE_SENT = 1;     // Tin nhắn gửi đi (bên phải)
    private static final int TYPE_RECEIVED = 2; // Tin nhắn nhận được (bên trái)
    
    private List<TinNhanBacSi> danhSachTinNhan;
    private SimpleDateFormat timeFormat;
    private boolean isDoctorView; // true nếu là view của bác sĩ
    
    public TinNhanBacSiAdapter(boolean isDoctorView) {
        this.danhSachTinNhan = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.isDoctorView = isDoctorView;
    }
    
    @Override
    public int getItemViewType(int position) {
        TinNhanBacSi tinNhan = danhSachTinNhan.get(position);
        
        // Xác định tin nhắn gửi đi hay nhận được dựa trên người đang xem
        if (isDoctorView) {
            // Bác sĩ view: tin nhắn của bác sĩ là gửi đi, của bệnh nhân là nhận được
            return tinNhan.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BAC_SI ? 
                   TYPE_SENT : TYPE_RECEIVED;
        } else {
            // Bệnh nhân view: tin nhắn của bệnh nhân là gửi đi, của bác sĩ là nhận được
            return tinNhan.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BENH_NHAN ? 
                   TYPE_SENT : TYPE_RECEIVED;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        if (viewType == TYPE_SENT) {
            // Tin nhắn gửi đi - sử dụng layout bên phải
            View view = inflater.inflate(R.layout.item_tin_nhan_benh_nhan, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            // Tin nhắn nhận được - sử dụng layout bên trái
            View view = inflater.inflate(R.layout.item_tin_nhan_bac_si, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TinNhanBacSi tinNhan = danhSachTinNhan.get(position);
        
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(tinNhan);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(tinNhan);
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
    
    // ViewHolder cho tin nhắn gửi đi (bên phải)
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNoiDung, tvThoiGian;
        private android.widget.ImageView ivSeenStatus;
        
        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoiDung = itemView.findViewById(R.id.tvNoiDung);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
            ivSeenStatus = itemView.findViewById(R.id.ivSeenStatus);
        }
        
        public void bind(TinNhanBacSi tinNhan) {
            tvNoiDung.setText(tinNhan.getNoiDung());
            if (tinNhan.getThoiGianGui() != null) {
                tvThoiGian.setText(timeFormat.format(tinNhan.getThoiGianGui().toDate()));
            }
            
            // Hiển thị trạng thái seen
            if (ivSeenStatus != null) {
                updateSeenStatus(tinNhan, ivSeenStatus);
            }
        }
        
        private void updateSeenStatus(TinNhanBacSi tinNhan, android.widget.ImageView ivSeenStatus) {
            if (tinNhan.getTrangThai() == TinNhanBacSi.TrangThaiTinNhan.DA_XEM) {
                // Đã xem - hiển thị check xanh
                ivSeenStatus.setImageResource(R.drawable.ic_check);
                ivSeenStatus.setColorFilter(androidx.core.content.ContextCompat.getColor(
                    ivSeenStatus.getContext(), R.color.primary));
                ivSeenStatus.setVisibility(View.VISIBLE);
            } else if (tinNhan.getTrangThai() == TinNhanBacSi.TrangThaiTinNhan.DA_NHAN) {
                // Đã nhận nhưng chưa xem - hiển thị check xám
                ivSeenStatus.setImageResource(R.drawable.ic_check);
                ivSeenStatus.setColorFilter(androidx.core.content.ContextCompat.getColor(
                    ivSeenStatus.getContext(), R.color.text_hint));
                ivSeenStatus.setVisibility(View.VISIBLE);
            } else {
                // Đang gửi hoặc chưa nhận - ẩn icon
                ivSeenStatus.setVisibility(View.GONE);
            }
        }
    }
    
    // ViewHolder cho tin nhắn nhận được (bên trái)
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNoiDung, tvThoiGian;
        private CircleImageView ivAvatar;
        
        public ReceivedMessageViewHolder(@NonNull View itemView) {
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
            
            // Hiển thị avatar phù hợp
            if (isDoctorView) {
                // Bác sĩ view: tin nhắn nhận được từ bệnh nhân
                ivAvatar.setImageResource(R.drawable.ic_patient);
            } else {
                // Bệnh nhân view: tin nhắn nhận được từ bác sĩ
                ivAvatar.setImageResource(R.drawable.ic_doctor);
            }
        }
    }
}