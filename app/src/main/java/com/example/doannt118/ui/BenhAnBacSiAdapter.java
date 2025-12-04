package com.example.doannt118.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BenhAnBacSiAdapter extends RecyclerView.Adapter<BenhAnBacSiAdapter.ViewHolder> {
    private Context context;
    private List<BenhAn> benhAnList;
    private SimpleDateFormat dateFormat;
    private FirestoreRepository repository;

    public BenhAnBacSiAdapter(Context context) {
        this.context = context;
        this.benhAnList = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.repository = new FirestoreRepository();
    }

    public void setData(List<BenhAn> list) {
        this.benhAnList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_benh_an_bac_si, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BenhAn benhAn = benhAnList.get(position);
        
        holder.tvMaBenhAn.setText(benhAn.getMaBenhAn());
        
        if (benhAn.getNgayKhamAsTimestamp() != null) {
            holder.tvNgayKham.setText(dateFormat.format(benhAn.getNgayKhamAsTimestamp().toDate()));
        } else if (benhAn.getNgayKham() instanceof String) {
            holder.tvNgayKham.setText((String) benhAn.getNgayKham());
        } else {
            holder.tvNgayKham.setText("N/A");
        }
        
        String chanDoan = benhAn.getChanDoan();
        if (chanDoan != null && !chanDoan.isEmpty()) {
            holder.tvChanDoan.setText(chanDoan);
            holder.tvTrangThai.setText("Đã khám");
            holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_completed);
        } else {
            holder.tvChanDoan.setText("Chưa chẩn đoán");
            holder.tvTrangThai.setText("Chờ khám");
            holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_pending);
        }
        
        // Load tên bệnh nhân
        loadBenhNhanInfo(benhAn.getMaBenhNhan(), holder.tvTenBenhNhan);
        
        holder.btnXem.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChiTietBenhAnActivity.class);
            intent.putExtra("maBenhAn", benhAn.getMaBenhAn());
            context.startActivity(intent);
        });
        
        holder.btnCapNhat.setOnClickListener(v -> {
            Intent intent = new Intent(context, CapNhatBenhAnActivity.class);
            intent.putExtra("maBenhAn", benhAn.getMaBenhAn());
            context.startActivity(intent);
        });
    }

    private void loadBenhNhanInfo(String maBenhNhan, TextView textView) {
        repository.getByField("BenhNhan", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    String hoTen = doc.getString("hoTen");
                    textView.setText(hoTen != null ? hoTen : "Không rõ");
                }
            },
            e -> textView.setText("Không rõ")
        );
    }

    @Override
    public int getItemCount() {
        return benhAnList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenBenhNhan, tvMaBenhAn, tvNgayKham, tvChanDoan, tvTrangThai;
        MaterialButton btnXem, btnCapNhat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenBenhNhan = itemView.findViewById(R.id.tvTenBenhNhan);
            tvMaBenhAn = itemView.findViewById(R.id.tvMaBenhAn);
            tvNgayKham = itemView.findViewById(R.id.tvNgayKham);
            tvChanDoan = itemView.findViewById(R.id.tvChanDoan);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            btnXem = itemView.findViewById(R.id.btnXem);
            btnCapNhat = itemView.findViewById(R.id.btnCapNhat);
        }
    }
}
