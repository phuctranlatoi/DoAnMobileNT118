package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.BenhNhan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BenhNhanSelectAdapter extends RecyclerView.Adapter<BenhNhanSelectAdapter.ViewHolder> {
    private List<BenhNhan> benhNhanList;
    private Set<String> selectedIds;

    public BenhNhanSelectAdapter(List<BenhNhan> benhNhanList) {
        this.benhNhanList = benhNhanList;
        this.selectedIds = new HashSet<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_benh_nhan_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BenhNhan bn = benhNhanList.get(position);
        holder.tvTenBenhNhan.setText(bn.getHoTen());
        holder.tvSoDienThoai.setText(bn.getSoDienThoai());
        
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedIds.contains(bn.getMaBenhNhan()));
        
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedIds.add(bn.getMaBenhNhan());
            } else {
                selectedIds.remove(bn.getMaBenhNhan());
            }
        });

        holder.itemView.setOnClickListener(v -> holder.checkBox.toggle());
    }

    @Override
    public int getItemCount() {
        return benhNhanList.size();
    }

    public List<BenhNhan> getSelectedBenhNhan() {
        List<BenhNhan> selected = new ArrayList<>();
        for (BenhNhan bn : benhNhanList) {
            if (selectedIds.contains(bn.getMaBenhNhan())) {
                selected.add(bn);
            }
        }
        return selected;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvTenBenhNhan, tvSoDienThoai;

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            tvTenBenhNhan = itemView.findViewById(R.id.tvTenBenhNhan);
            tvSoDienThoai = itemView.findViewById(R.id.tvSoDienThoai);
        }
    }
}
