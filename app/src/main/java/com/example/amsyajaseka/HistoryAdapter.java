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

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<Order> orders = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", new Locale("id", "ID"));
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Order currentOrder = orders.get(position);
        holder.tvName.setText(currentOrder.name);
        
        String dateStr = "Selesai: " + dateFormat.format(new Date(currentOrder.completionDate));
        holder.tvDate.setText(dateStr);

        double total = currentOrder.weight * currentOrder.pricePerKg;
        holder.tvTotal.setText(currencyFormat.format(total));

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), OrderDetailActivity.class);
            intent.putExtra("ORDER_ID", currentOrder.id);
            intent.putExtra("IS_HISTORY_MODE", true);
            v.getContext().startActivity(intent);
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

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvDate;
        private TextView tvTotal;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHistoryName);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvTotal = itemView.findViewById(R.id.tvHistoryTotal);
        }
    }
}
