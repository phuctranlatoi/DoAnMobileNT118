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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BenhAnAdapter extends RecyclerView.Adapter<BenhAnAdapter.ViewHolder> {
    private Context context;
    private List<BenhAn> benhAnList;
    private SimpleDateFormat dateFormat;
    private com.example.doannt118.repository.FirestoreRepository repository;

    public BenhAnAdapter(Context context) {
        this.context = context;
        this.benhAnList = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.repository = new com.example.doannt118.repository.FirestoreRepository();
    }

    public void setData(List<BenhAn> list) {
        this.benhAnList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_benh_an, parent, false);
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
        
        holder.tvChanDoan.setText("Chẩn đoán: " + (benhAn.getChanDoan() != null ? benhAn.getChanDoan() : "Chưa có"));
        
        // Load tên bác sĩ
        loadBacSiInfo(benhAn.getMaBacSi(), holder.tvBacSi);
        
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChiTietBenhAnActivity.class);
            intent.putExtra("maBenhAn", benhAn.getMaBenhAn());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return benhAnList.size();
    }
    
    private void loadBacSiInfo(String maBacSi, TextView textView) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            textView.setText("Bác sĩ: N/A");
            return;
        }
        
        repository.getByField("BacSi", "maBacSi", maBacSi,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    com.google.firebase.firestore.DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    String hoTen = doc.getString("hoTen");
                    textView.setText("Bác sĩ: " + (hoTen != null ? hoTen : maBacSi));
                } else {
                    textView.setText("Bác sĩ: " + maBacSi);
                }
            },
            e -> textView.setText("Bác sĩ: " + maBacSi)
        );
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaBenhAn, tvNgayKham, tvChanDoan, tvBacSi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaBenhAn = itemView.findViewById(R.id.tvMaBenhAn);
            tvNgayKham = itemView.findViewById(R.id.tvNgayKham);
            tvChanDoan = itemView.findViewById(R.id.tvChanDoan);
            tvBacSi = itemView.findViewById(R.id.tvBacSi);
        }
    }
}
