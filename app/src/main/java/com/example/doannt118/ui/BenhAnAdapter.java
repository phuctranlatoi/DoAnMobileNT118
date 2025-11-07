package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BenhAnAdapter extends RecyclerView.Adapter<BenhAnAdapter.BenhAnViewHolder> {

    private List<BenhAn> benhAnList;
    private OnBenhAnClickListener listener;
    private FirestoreRepository repo;

    public interface OnBenhAnClickListener {
        void onBenhAnClick(BenhAn benhAn);
    }

    public BenhAnAdapter(List<BenhAn> benhAnList, OnBenhAnClickListener listener) {
        this.benhAnList = benhAnList;
        this.listener = listener;
        this.repo = new FirestoreRepository();
    }

    @NonNull
    @Override
    public BenhAnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_benhan, parent, false);
        return new BenhAnViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BenhAnViewHolder holder, int position) {
        BenhAn benhAn = benhAnList.get(position);
        holder.tvMaBenhAn.setText("Mã Bệnh Án: " + (benhAn.getMaBenhAn() != null ? benhAn.getMaBenhAn() : "N/A"));
        holder.tvChanDoan.setText("Chẩn Đoán: " + (benhAn.getChanDoan() != null ? benhAn.getChanDoan() : "N/A"));
        holder.tvNgayKham.setText(benhAn.getNgayKham() != null
                ? new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(benhAn.getNgayKham().toDate())
                : "N/A");

        // Load patient name
        repo.getByField("BenhNhan", "maBenhNhan", benhAn.getMaBenhNhan(),
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        BenhNhan benhNhan = doc.toObject(BenhNhan.class);
                        if (benhNhan != null) {
                            holder.tvMaBenhNhan.setText("Bệnh nhân: " + benhNhan.getHoTen());
                        } else {
                            holder.tvMaBenhNhan.setText("Mã Bệnh Nhân: " + (benhAn.getMaBenhNhan() != null ? benhAn.getMaBenhNhan() : "N/A"));
                        }
                    } else {
                        holder.tvMaBenhNhan.setText("Mã Bệnh Nhân: " + (benhAn.getMaBenhNhan() != null ? benhAn.getMaBenhNhan() : "N/A"));
                    }
                },
                e -> holder.tvMaBenhNhan.setText("Mã Bệnh Nhân: " + (benhAn.getMaBenhNhan() != null ? benhAn.getMaBenhNhan() : "N/A")));

        holder.itemView.setOnClickListener(v -> listener.onBenhAnClick(benhAn));
    }

    @Override
    public int getItemCount() {
        return benhAnList.size();
    }

    static class BenhAnViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaBenhAn, tvMaBenhNhan, tvChanDoan, tvNgayKham;

        public BenhAnViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaBenhAn = itemView.findViewById(R.id.tvMaBenhAn);
            tvMaBenhNhan = itemView.findViewById(R.id.tvMaBenhNhan);
            tvChanDoan = itemView.findViewById(R.id.tvChanDoan);
            tvNgayKham = itemView.findViewById(R.id.tvNgayKham);
        }
    }
}