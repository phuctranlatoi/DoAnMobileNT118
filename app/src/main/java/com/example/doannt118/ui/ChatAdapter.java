package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.ChatMessage;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;
    private static final int VIEW_TYPE_BOT_ACTIONS = 3;
    private static final int VIEW_TYPE_DOCTOR_CAROUSEL = 4;
    
    private List<ChatMessage> messages;
    private OnActionClickListener actionClickListener;
    private OnDoctorSelectListener doctorSelectListener;
    
    public interface OnActionClickListener {
        void onActionClick(ChatMessage.ActionButton action);
    }
    
    public interface OnDoctorSelectListener {
        void onDoctorSelect(BacSi doctor);
    }
    
    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }
    
    public void setOnActionClickListener(OnActionClickListener listener) {
        this.actionClickListener = listener;
    }
    
    public void setOnDoctorSelectListener(OnDoctorSelectListener listener) {
        this.doctorSelectListener = listener;
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        
        if (message.getType() == ChatMessage.MessageType.USER) {
            return VIEW_TYPE_USER;
        } else if (message.getType() == ChatMessage.MessageType.ACTION_BUTTONS) {
            return VIEW_TYPE_BOT_ACTIONS;
        } else if (message.getType() == ChatMessage.MessageType.DOCTOR_CARD) {
            return VIEW_TYPE_DOCTOR_CAROUSEL;
        } else {
            return VIEW_TYPE_BOT;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        switch (viewType) {
            case VIEW_TYPE_USER:
                return new UserMessageViewHolder(
                    inflater.inflate(R.layout.item_chat_user, parent, false));
            case VIEW_TYPE_BOT_ACTIONS:
                return new BotActionsViewHolder(
                    inflater.inflate(R.layout.item_chat_bot_actions, parent, false));
            case VIEW_TYPE_DOCTOR_CAROUSEL:
                return new DoctorCarouselViewHolder(
                    inflater.inflate(R.layout.item_chat_doctor_carousel, parent, false));
            default:
                return new BotMessageViewHolder(
                    inflater.inflate(R.layout.item_chat_bot, parent, false));
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof BotActionsViewHolder) {
            ((BotActionsViewHolder) holder).bind(message);
        } else if (holder instanceof DoctorCarouselViewHolder) {
            ((DoctorCarouselViewHolder) holder).bind(message);
        } else if (holder instanceof BotMessageViewHolder) {
            ((BotMessageViewHolder) holder).bind(message);
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    // ============================================
    // VIEW HOLDERS
    // ============================================
    
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        
        UserMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
        
        void bind(ChatMessage message) {
            tvMessage.setText(message.getText());
        }
    }
    
    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        
        BotMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
        
        void bind(ChatMessage message) {
            tvMessage.setText(message.getText());
        }
    }
    
    class BotActionsViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        LinearLayout actionButtonsContainer;
        
        BotActionsViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            actionButtonsContainer = itemView.findViewById(R.id.actionButtonsContainer);
        }
        
        void bind(ChatMessage message) {
            tvMessage.setText(message.getText());
            
            // Clear previous buttons
            actionButtonsContainer.removeAllViews();
            
            if (message.hasActionButtons()) {
                actionButtonsContainer.setVisibility(View.VISIBLE);
                
                for (ChatMessage.ActionButton action : message.getActionButtons()) {
                    MaterialButton button = (MaterialButton) LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.item_action_button, actionButtonsContainer, false);
                    
                    button.setText(action.getText());
                    
                    // Set icon if available
                    if (action.getIconResId() != 0) {
                        button.setIconResource(action.getIconResId());
                    }
                    
                    // Style primary button differently
                    if (action.isPrimary()) {
                        button.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(
                                itemView.getContext().getResources().getColor(R.color.colorPrimary)));
                        button.setTextColor(android.graphics.Color.WHITE);
                        button.setIconTint(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
                    }
                    
                    button.setOnClickListener(v -> {
                        if (actionClickListener != null) {
                            actionClickListener.onActionClick(action);
                        }
                    });
                    
                    actionButtonsContainer.addView(button);
                }
            } else {
                actionButtonsContainer.setVisibility(View.GONE);
            }
        }
    }
    
    class DoctorCarouselViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        LinearLayout doctorCardsContainer;
        
        DoctorCarouselViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            doctorCardsContainer = itemView.findViewById(R.id.doctorCardsContainer);
        }
        
        void bind(ChatMessage message) {
            tvMessage.setText(message.getText());
            
            // Clear previous cards
            doctorCardsContainer.removeAllViews();
            
            Object cardData = message.getCardData();
            if (cardData instanceof List) {
                @SuppressWarnings("unchecked")
                List<BacSi> doctors = (List<BacSi>) cardData;
                
                for (BacSi doctor : doctors) {
                    View cardView = LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.item_doctor_card, doctorCardsContainer, false);
                    
                    bindDoctorCard(cardView, doctor);
                    doctorCardsContainer.addView(cardView);
                }
            }
        }
        
        private void bindDoctorCard(View cardView, BacSi doctor) {
            TextView tvDoctorName = cardView.findViewById(R.id.tvDoctorName);
            TextView tvSpecialty = cardView.findViewById(R.id.tvSpecialty);
            TextView tvExperience = cardView.findViewById(R.id.tvExperience);
            TextView tvSchedule = cardView.findViewById(R.id.tvSchedule);
            View scheduleContainer = cardView.findViewById(R.id.scheduleContainer);
            MaterialButton btnBook = cardView.findViewById(R.id.btnBookDoctor);
            
            tvDoctorName.setText("BS. " + doctor.getHoTen());
            tvSpecialty.setText(doctor.getChuyenKhoa());
            tvExperience.setText(doctor.getNamKinhNghiem() + " năm kinh nghiệm");
            
            // Show schedule if available
            if (doctor.getCaLamViec() != null && !doctor.getCaLamViec().isEmpty()) {
                scheduleContainer.setVisibility(View.VISIBLE);
                String scheduleText = formatSchedule(doctor.getCaLamViec());
                tvSchedule.setText(scheduleText);
            } else {
                scheduleContainer.setVisibility(View.GONE);
            }
            
            btnBook.setOnClickListener(v -> {
                if (doctorSelectListener != null) {
                    doctorSelectListener.onDoctorSelect(doctor);
                }
            });
        }
        
        private String formatSchedule(String caLamViec) {
            switch (caLamViec) {
                case "SANG":
                    return "Ca sáng: 8:00 - 11:30";
                case "CHIEU":
                    return "Ca chiều: 14:00 - 17:30";
                case "CA_NGAY":
                    return "Cả ngày: 8:00 - 17:30";
                default:
                    return caLamViec;
            }
        }
    }
}
