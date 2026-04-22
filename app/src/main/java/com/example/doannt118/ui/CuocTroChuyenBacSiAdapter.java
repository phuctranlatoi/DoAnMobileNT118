package com.example.doannt118.ui;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.CuocTroChuyenBacSi;
import com.example.doannt118.model.TinNhanBacSi;
import de.hdodenhof.circleimageview.CircleImageView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CuocTroChuyenBacSiAdapter extends RecyclerView.Adapter<CuocTroChuyenBacSiAdapter.ViewHolder> {
    
    private List<CuocTroChuyenBacSi> danhSachCuocTroChuyenBacSi;
    private OnItemClickListener listener;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;
    
    public interface OnItemClickListener {
        void onItemClick(CuocTroChuyenBacSi cuocTroChuyenBacSi);
    }
    
    public CuocTroChuyenBacSiAdapter(OnItemClickListener listener) {
        this.danhSachCuocTroChuyenBacSi = new ArrayList<>();
        this.listener = listener;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cuoc_tro_chuyen_bac_si, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CuocTroChuyenBacSi cuocTroChuyenBacSi = danhSachCuocTroChuyenBacSi.get(position);
        holder.bind(cuocTroChuyenBacSi);
    }
    
    @Override
    public int getItemCount() {
        return danhSachCuocTroChuyenBacSi.size();
    }
    
    public void setData(List<CuocTroChuyenBacSi> danhSachCuocTroChuyenBacSi) {
        this.danhSachCuocTroChuyenBacSi = danhSachCuocTroChuyenBacSi;
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivAvatarBenhNhan;
        private TextView tvTenBenhNhan, tvTinNhanCuoi, tvThoiGianCuoi, tvSoTinNhanChuaDoc;
        private ImageView ivUnreadDot, ivSeenStatusCuoi;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatarBenhNhan = itemView.findViewById(R.id.ivAvatarBenhNhan);
            tvTenBenhNhan = itemView.findViewById(R.id.tvTenBenhNhan);
            tvTinNhanCuoi = itemView.findViewById(R.id.tvTinNhanCuoi);
            tvThoiGianCuoi = itemView.findViewById(R.id.tvThoiGianCuoi);
            tvSoTinNhanChuaDoc = itemView.findViewById(R.id.tvSoTinNhanChuaDoc);
            ivUnreadDot = itemView.findViewById(R.id.ivUnreadDot);
            ivSeenStatusCuoi = itemView.findViewById(R.id.ivSeenStatusCuoi);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(danhSachCuocTroChuyenBacSi.get(position));
                    }
                }
            });
        }
        
        public void bind(CuocTroChuyenBacSi cuocTroChuyenBacSi) {
            boolean hasUnreadMessages = cuocTroChuyenBacSi.getSoTinNhanChuaDoc() > 0;
            
            // Hiển thị tên bệnh nhân - in đậm nếu có tin nhắn chưa đọc
            tvTenBenhNhan.setText(cuocTroChuyenBacSi.getTenBenhNhan());
            if (hasUnreadMessages) {
                tvTenBenhNhan.setTypeface(null, Typeface.BOLD);
                tvTenBenhNhan.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            } else {
                tvTenBenhNhan.setTypeface(null, Typeface.NORMAL);
                tvTenBenhNhan.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
            }
            
            // Hiển thị tin nhắn cuối với prefix - in đậm nếu có tin nhắn chưa đọc
            String tinNhanCuoi = cuocTroChuyenBacSi.getTinNhanCuoi();
            if (cuocTroChuyenBacSi.isLaBacSiGuiCuoi()) {
                tvTinNhanCuoi.setText("Bạn: " + tinNhanCuoi);
            } else {
                // Không thêm prefix cho tin nhắn hệ thống hoặc tin nhắn từ bệnh nhân
                tvTinNhanCuoi.setText(tinNhanCuoi);
            }
            
            if (hasUnreadMessages) {
                tvTinNhanCuoi.setTypeface(null, Typeface.BOLD);
                tvTinNhanCuoi.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            } else {
                tvTinNhanCuoi.setTypeface(null, Typeface.NORMAL);
                tvTinNhanCuoi.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            }
            
            // Hiển thị thời gian - in đậm nếu có tin nhắn chưa đọc
            if (cuocTroChuyenBacSi.getThoiGianCuoi() != null) {
                Date thoiGian = cuocTroChuyenBacSi.getThoiGianCuoi().toDate();
                String thoiGianText = formatTime(thoiGian);
                tvThoiGianCuoi.setText(thoiGianText);
                
                if (hasUnreadMessages) {
                    tvThoiGianCuoi.setTypeface(null, Typeface.BOLD);
                    tvThoiGianCuoi.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));
                } else {
                    tvThoiGianCuoi.setTypeface(null, Typeface.NORMAL);
                    tvThoiGianCuoi.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_hint));
                }
            }
            
            // Hiển thị badge số tin nhắn chưa đọc
            if (hasUnreadMessages) {
                tvSoTinNhanChuaDoc.setVisibility(View.VISIBLE);
                tvSoTinNhanChuaDoc.setText(String.valueOf(cuocTroChuyenBacSi.getSoTinNhanChuaDoc()));
                
                // Hiển thị dấu chấm xanh cho tin nhắn chưa đọc
                if (ivUnreadDot != null) {
                    ivUnreadDot.setVisibility(View.VISIBLE);
                }
            } else {
                tvSoTinNhanChuaDoc.setVisibility(View.GONE);
                if (ivUnreadDot != null) {
                    ivUnreadDot.setVisibility(View.GONE);
                }
            }
            
            // Hiển thị trạng thái seen cho tin nhắn cuối nếu là tin nhắn gửi đi
            if (ivSeenStatusCuoi != null && cuocTroChuyenBacSi.isLaBacSiGuiCuoi()) {
                updateSeenStatusIcon(cuocTroChuyenBacSi.getTrangThaiTinNhanCuoi(), ivSeenStatusCuoi);
            } else if (ivSeenStatusCuoi != null) {
                ivSeenStatusCuoi.setVisibility(View.GONE);
            }
            
            // Load avatar (có thể dùng Glide nếu có URL)
            ivAvatarBenhNhan.setImageResource(R.drawable.ic_patient);
        }
        
        private void updateSeenStatusIcon(TinNhanBacSi.TrangThaiTinNhan trangThai, ImageView ivSeenStatus) {
            if (trangThai == TinNhanBacSi.TrangThaiTinNhan.DA_XEM) {
                // Đã xem - hiển thị check xanh
                ivSeenStatus.setImageResource(R.drawable.ic_check);
                ivSeenStatus.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.primary));
                ivSeenStatus.setVisibility(View.VISIBLE);
            } else if (trangThai == TinNhanBacSi.TrangThaiTinNhan.DA_NHAN) {
                // Đã nhận nhưng chưa xem - hiển thị check xám
                ivSeenStatus.setImageResource(R.drawable.ic_check);
                ivSeenStatus.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_hint));
                ivSeenStatus.setVisibility(View.VISIBLE);
            } else {
                // Đang gửi hoặc chưa nhận - ẩn icon
                ivSeenStatus.setVisibility(View.GONE);
            }
        }
        
        private String formatTime(Date date) {
            Calendar today = Calendar.getInstance();
            Calendar messageTime = Calendar.getInstance();
            messageTime.setTime(date);
            
            // Nếu cùng ngày thì hiển thị giờ
            if (today.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)) {
                return timeFormat.format(date);
            } else {
                // Nếu khác ngày thì hiển thị ngày/tháng
                return dateFormat.format(date);
            }
        }
    }
}