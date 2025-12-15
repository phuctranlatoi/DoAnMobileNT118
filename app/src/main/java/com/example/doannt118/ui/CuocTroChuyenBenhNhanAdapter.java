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
import com.example.doannt118.model.CuocTroChuyenBenhNhan;
import de.hdodenhof.circleimageview.CircleImageView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CuocTroChuyenBenhNhanAdapter extends RecyclerView.Adapter<CuocTroChuyenBenhNhanAdapter.ViewHolder> {
    
    private List<CuocTroChuyenBenhNhan> danhSachCuocTroChuyenBenhNhan;
    private OnItemClickListener listener;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;
    
    public interface OnItemClickListener {
        void onItemClick(CuocTroChuyenBenhNhan cuocTroChuyenBenhNhan);
    }
    
    public CuocTroChuyenBenhNhanAdapter(OnItemClickListener listener) {
        this.danhSachCuocTroChuyenBenhNhan = new ArrayList<>();
        this.listener = listener;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cuoc_tro_chuyen_benh_nhan, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CuocTroChuyenBenhNhan cuocTroChuyenBenhNhan = danhSachCuocTroChuyenBenhNhan.get(position);
        holder.bind(cuocTroChuyenBenhNhan);
    }
    
    @Override
    public int getItemCount() {
        return danhSachCuocTroChuyenBenhNhan.size();
    }
    
    public void setData(List<CuocTroChuyenBenhNhan> danhSachCuocTroChuyenBenhNhan) {
        this.danhSachCuocTroChuyenBenhNhan = danhSachCuocTroChuyenBenhNhan;
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivAvatarBacSi;
        private TextView tvTenBacSi, tvTinNhanCuoi, tvThoiGianCuoi, tvSoTinNhanChuaDoc;
        private ImageView ivUnreadDot;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatarBacSi = itemView.findViewById(R.id.ivAvatarBacSi);
            tvTenBacSi = itemView.findViewById(R.id.tvTenBacSi);
            tvTinNhanCuoi = itemView.findViewById(R.id.tvTinNhanCuoi);
            tvThoiGianCuoi = itemView.findViewById(R.id.tvThoiGianCuoi);
            tvSoTinNhanChuaDoc = itemView.findViewById(R.id.tvSoTinNhanChuaDoc);
            ivUnreadDot = itemView.findViewById(R.id.ivUnreadDot);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(danhSachCuocTroChuyenBenhNhan.get(position));
                    }
                }
            });
        }
        
        public void bind(CuocTroChuyenBenhNhan cuocTroChuyenBenhNhan) {
            boolean hasUnreadMessages = cuocTroChuyenBenhNhan.getSoTinNhanChuaDoc() > 0;
            
            // Hiển thị tên bác sĩ - in đậm nếu có tin nhắn chưa đọc
            tvTenBacSi.setText(cuocTroChuyenBenhNhan.getTenBacSi());
            if (hasUnreadMessages) {
                tvTenBacSi.setTypeface(null, Typeface.BOLD);
                tvTenBacSi.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            } else {
                tvTenBacSi.setTypeface(null, Typeface.NORMAL);
                tvTenBacSi.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
            }
            
            // Hiển thị tin nhắn cuối với prefix - in đậm nếu có tin nhắn chưa đọc
            String tinNhanCuoi = cuocTroChuyenBenhNhan.getTinNhanCuoi();
            if (cuocTroChuyenBenhNhan.isLaBenhNhanGuiCuoi()) {
                tvTinNhanCuoi.setText("Bạn: " + tinNhanCuoi);
            } else {
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
            if (cuocTroChuyenBenhNhan.getThoiGianCuoi() != null) {
                Date thoiGian = cuocTroChuyenBenhNhan.getThoiGianCuoi().toDate();
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
                tvSoTinNhanChuaDoc.setText(String.valueOf(cuocTroChuyenBenhNhan.getSoTinNhanChuaDoc()));
                
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
            
            // Load avatar (có thể dùng Glide nếu có URL)
            ivAvatarBacSi.setImageResource(R.drawable.ic_doctor);
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