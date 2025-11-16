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
        holder.tvMaBenhAn.setText("Mã: " + (benhAn.getMaBenhAn() != null ? benhAn.getMaBenhAn() : "N/A"));
        holder.tvChanDoan.setText(benhAn.getChanDoan() != null ? benhAn.getChanDoan() : "Chưa có chẩn đoán");
        holder.tvNgayKham.setText(benhAn.getNgayKham() != null
                ? new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(benhAn.getNgayKham().toDate())
                : "N/A");

        // Load patient name
        if (benhAn.getMaBenhNhan() != null && !benhAn.getMaBenhNhan().isEmpty()) {
            repo.getByField("BenhNhan", "maBenhNhan", benhAn.getMaBenhNhan(),
                    querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                            BenhNhan benhNhan = doc.toObject(BenhNhan.class);
                            if (benhNhan != null && benhNhan.getHoTen() != null) {
                                holder.tvMaBenhNhan.setText("BN: " + benhNhan.getHoTen());
                            } else {
                                holder.tvMaBenhNhan.setText("BN: " + benhAn.getMaBenhNhan());
                            }
                        } else {
                            holder.tvMaBenhNhan.setText("BN: " + benhAn.getMaBenhNhan());
                        }
                    },
                    e -> holder.tvMaBenhNhan.setText("BN: " + benhAn.getMaBenhNhan()));
        } else {
            holder.tvMaBenhNhan.setText("BN: N/A");
        }

        // Click on item or edit icon
        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onBenhAnClick(benhAn);
            }
        };
        
        holder.itemView.setOnClickListener(clickListener);
        if (holder.btnEdit != null) {
            holder.btnEdit.setOnClickListener(clickListener);
        }
    }

    @Override
    public int getItemCount() {
        return benhAnList.size();
    }

    static class BenhAnViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaBenhAn, tvMaBenhNhan, tvChanDoan, tvNgayKham;
        View btnEdit;

        public BenhAnViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaBenhAn = itemView.findViewById(R.id.tvMaBenhAn);
            tvMaBenhNhan = itemView.findViewById(R.id.tvMaBenhNhan);
            tvChanDoan = itemView.findViewById(R.id.tvChanDoan);
            tvNgayKham = itemView.findViewById(R.id.tvNgayKham);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}