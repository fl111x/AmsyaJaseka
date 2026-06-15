package com.example.amsyajaseka;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<File> reportFiles = new ArrayList<>();
    private Context context;

    public ReportAdapter(Context context) {
        this.context = context;
    }

    public void setReportFiles(List<File> reportFiles) {
        this.reportFiles = reportFiles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        File file = reportFiles.get(position);
        holder.tvReportName.setText(file.getName());
        holder.tvReportPath.setText(file.getAbsolutePath());

        holder.itemView.setOnClickListener(v -> {
            shareFile(file);
        });
    }

    @Override
    public int getItemCount() {
        return reportFiles.size();
    }

    private void shareFile(File file) {
        try {
            Uri uri;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
            } else {
                uri = Uri.fromFile(file);
            }

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            context.startActivity(Intent.createChooser(intent, "Bagikan Laporan CSV"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Gagal membagikan file", Toast.LENGTH_SHORT).show();
        }
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportName, tvReportPath;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportName = itemView.findViewById(R.id.tvReportName);
            tvReportPath = itemView.findViewById(R.id.tvReportPath);
        }
    }
}
