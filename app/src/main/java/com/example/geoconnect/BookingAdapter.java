package com.example.geoconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;
    private OnBookingActionListener actionListener;

    public interface OnBookingActionListener {
        void onAccept(Booking booking);
        void onDecline(Booking booking);
    }

    public BookingAdapter(List<Booking> bookingList, OnBookingActionListener actionListener) {
        this.bookingList = bookingList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_request, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.txtInitials.setText(booking.getInitials());
        holder.txtCustomerName.setText(booking.getCustomerName());
        holder.txtServiceTitle.setText(booking.getServiceTitle());
        holder.txtPrice.setText(booking.getPrice());
        holder.txtDateTime.setText(booking. getDateTime());
        holder.txtLocation.setText(booking.getLocation());

        if ("Pending".equals(booking.getStatus())) {
            holder.layoutActionButtons.setVisibility(View.VISIBLE);
            holder.btnAccept.setOnClickListener(v -> actionListener.onAccept(booking));
            holder.btnDecline.setOnClickListener(v -> actionListener.onDecline(booking));
        } else {
            holder.layoutActionButtons.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView txtInitials, txtCustomerName, txtServiceTitle, txtPrice, txtDateTime, txtLocation, btnAccept, btnDecline;
        ViewGroup layoutActionButtons;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtInitials = itemView.findViewById(R.id.txtInitials);
            txtCustomerName = itemView.findViewById(R.id.txtCustomerName);
            txtServiceTitle = itemView.findViewById(R.id.txtServiceTitle);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtDateTime = itemView.findViewById(R.id.txtDateTime);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
        }
    }
}