package com.example.activereads.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activereads.Model.LibraryItems;
import com.example.activereads.R;
import com.example.activereads.Subscription;
import com.example.activereads.ViewIntent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class RecyclerViewAdapterLibrary extends RecyclerView.Adapter<RecyclerViewAdapterLibrary.ViewHolder> {
    Context context;
    List<LibraryItems> list;
    FirebaseUser currentUser;
    FirebaseFirestore db;

    public RecyclerViewAdapterLibrary(Context context, List<LibraryItems> list) {
        this.context = context;
        this.list = list;
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        db= FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public RecyclerViewAdapterLibrary.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_library,parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapterLibrary.ViewHolder holder, int position) {
        holder.book_name.setText(list.get(position).getBookName());
        holder.category.setText(list.get(position).getCategory());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView book_name;
        TextView category;
        ImageView pdf;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            book_name=itemView.findViewById(R.id.book_name);
            category=itemView.findViewById(R.id.category);
            pdf=itemView.findViewById(R.id.book);
        }
        @Override
        public void onClick(View view) {
            Toast.makeText(context, "Clicked", Toast.LENGTH_LONG).show();
            db.collection("Users").whereEqualTo("User_Name",currentUser.getEmail()).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for(QueryDocumentSnapshot documentSnapshot:task.getResult()){
                        if(documentSnapshot.get("has_paid").toString().equals("NO")){
                            if(list.get(getAdapterPosition()).getCategory().equalsIgnoreCase("Paid")){
                                Intent intent=new Intent(context, Subscription.class);
                                context.startActivity(intent);
                            }else{
                                // Open the Item;
                                openBook();
                            }
                        }else{
                            // Open the Item;
                            openBook();
                        }
                    }
                }
            });
        }
        private void openBook(){
            Intent viewIntent=new Intent(context, ViewIntent.class);
            viewIntent.putExtra("Link",list.get(getAdapterPosition()).getLink());
            context.startActivity(viewIntent);
        }
    }
}
