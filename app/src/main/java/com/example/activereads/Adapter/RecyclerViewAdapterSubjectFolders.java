package com.example.activereads.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.activereads.R;
import com.example.activereads.UploadStudyMaterials;

import java.util.List;

public class RecyclerViewAdapterSubjectFolders extends RecyclerView.Adapter<RecyclerViewAdapterSubjectFolders.ViewHolder> {
    Context context;
    List<String> list;

    public RecyclerViewAdapterSubjectFolders(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerViewAdapterSubjectFolders.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_folder,parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapterSubjectFolders.ViewHolder holder, int position) {
        holder.subject.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView folderImage;
        TextView subject;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            folderImage=itemView.findViewById(R.id.folderImage);
            subject=itemView.findViewById(R.id.subject);
        }

        @Override
        public void onClick(View view) {
            Intent subjectNotes=new Intent(context, UploadStudyMaterials.class);
            subjectNotes.putExtra("Subject_Name", list.get(getAdapterPosition()));
            context.startActivity(subjectNotes);
        }
    }
}
