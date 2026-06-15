package com.example.amsyajaseka;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amsyajaseka.data.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(itemView);
    }

    public interface OnItemClickListener {
        void onItemClick(Order order);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order currentOrder = orders.get(position);
        holder.tvName.setText(currentOrder.name);
        holder.tvAddress.setText(currentOrder.address);
        
        String dateStr = dateFormat.format(new Date(currentOrder.date));
        holder.tvDate.setText(dateStr);

        String weightPriceStr = currentOrder.weight + " Kg x " + currencyFormat.format(currentOrder.pricePerKg);
        holder.tvWeightPrice.setText(weightPriceStr);

        double total = currentOrder.weight * currentOrder.pricePerKg;
        holder.tvTotal.setText(currencyFormat.format(total));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentOrder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvDate;
        private TextView tvAddress;
        private TextView tvWeightPrice;
        private TextView tvTotal;

        public OrderViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvWeightPrice = itemView.findViewById(R.id.tvWeightPrice);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }
    }
}
