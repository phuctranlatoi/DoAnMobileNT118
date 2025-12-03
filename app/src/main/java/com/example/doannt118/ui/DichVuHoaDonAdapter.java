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
import com.example.doannt118.model.ChiTietHoaDon;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DichVuHoaDonAdapter extends RecyclerView.Adapter<DichVuHoaDonAdapter.ViewHolder> {
    private Context context;
    private List<ChiTietHoaDon> dichVuList;
    private OnDeleteClickListener deleteListener;
    private NumberFormat currencyFormat;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public DichVuHoaDonAdapter(Context context, List<ChiTietHoaDon> dichVuList, 
                              OnDeleteClickListener deleteListener) {
        this.context = context;
        this.dichVuList = dichVuList != null ? dichVuList : new ArrayList<>();
        this.deleteListener = deleteListener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dich_vu_hoa_don, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChiTietHoaDon dichVu = dichVuList.get(position);
        
        holder.tvTenDichVu.setText(dichVu.getTenDichVu());
        
        String chiTiet = "SL: " + dichVu.getSoLuong() + " • Đơn giá: " + 
                        currencyFormat.format(dichVu.getDonGia());
        holder.tvChiTiet.setText(chiTiet);
        
        holder.tvThanhTien.setText("Thành tiền: " + currencyFormat.format(dichVu.getThanhTien()));
        
        holder.ivDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dichVuList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenDichVu, tvChiTiet, tvThanhTien;
        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenDichVu = itemView.findViewById(R.id.tvTenDichVu);
            tvChiTiet = itemView.findViewById(R.id.tvChiTiet);
            tvThanhTien = itemView.findViewById(R.id.tvThanhTien);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
