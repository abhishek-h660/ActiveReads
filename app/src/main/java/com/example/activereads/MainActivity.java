package com.example.activereads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.service.controls.actions.FloatAction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.activereads.Adapter.RecyclerViewAdapterLibrary;
import com.example.activereads.Adapter.RecyclerViewAdapterSubjectFolders;
import com.example.activereads.Model.LibraryItems;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    String collegeName;
    String semester;
    String stream;
    String email;
    TextView toolbar_title;
    Button subscription;
    ImageView dp;
    EditText createFolderName;
    Button createFolderButton;
    Button createFolderButtonCancel;
    RecyclerView recyclerView;
    RecyclerViewAdapterSubjectFolders recyclerViewAdapterSubjectFolders;

    List<String> stringList;
    FloatingActionButton createFolder;
    StorageReference storageReference;
    FirebaseUser current_user;
    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialise();
        if(current_user==null || !current_user.isEmailVerified()) {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
        else if(current_user.isEmailVerified()){
            Toast.makeText(MainActivity.this, "Signed In As:"+current_user.getEmail(), Toast.LENGTH_LONG).show();
            db.collection("Users").document(current_user.getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.contains("Profile_Picture")) {
                        storageReference = FirebaseStorage.getInstance().getReference(current_user.getEmail()).child("Profile_Picture");
                        storageReference.getBytes(2024l * 2024l).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                dp.setImageBitmap(bitmap);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Profile Picture Retrieve failed", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    email=current_user.getEmail();
                    stream=documentSnapshot.get("Stream").toString();
                    semester=documentSnapshot.get("Semester").toString();
                    collegeName=documentSnapshot.get("College_Name").toString();
                    setAvailable();
                }
            });
        }

        db.collection("Team").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(current_user!=null && current_user.isEmailVerified()){
                    setAvailable();
                }
            }
        });

        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editProfile=new Intent(MainActivity.this, EditProfile.class);
                startActivity(editProfile);
            }
        });

        subscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent subscriptionIntent=new Intent(MainActivity.this, Subscription.class);
                startActivity(subscriptionIntent);
            }
        });

        createFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFolderButton.setVisibility(View.VISIBLE);
                createFolderButtonCancel.setVisibility(View.VISIBLE);
                createFolderName.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.INVISIBLE);
            }
        });

        createFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name=createFolderName.getText().toString();
                if(!name.isEmpty() && name.indexOf('/')<0 && name.indexOf('?')<0){
                    db.collection("Users").document(current_user.getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Map<String, String> hashName=new HashMap<>();
                            hashName.put("Folder_Name", name);
                            db.collection("Team/College_Ambassador/"+collegeName+"/"+semester+"/"+stream+"/Folder/Subjects").add(hashName).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    setAvailable();
                                }
                            });
                        }
                    });
                }else{
                    Toast.makeText(MainActivity.this, "Enter Appropriate Folder Name", Toast.LENGTH_LONG).show();
                }
            }
        });

        createFolderButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAvailable();
            }
        });
    }

    private void initialise(){
        toolbar_title=findViewById(R.id.toolbar_title);
        subscription=findViewById(R.id.subscriptions);
        dp=findViewById(R.id.dp);
        createFolder=findViewById(R.id.createFolder);
        recyclerView=findViewById(R.id.recyclerViewNotes);
        db=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();
        current_user=auth.getCurrentUser();
        createFolderName=findViewById(R.id.createFolderName);
        createFolderButton=findViewById(R.id.createFolderButton);
        createFolderButtonCancel=findViewById(R.id.createFolderButtonCancel);
    }

    private void setAvailable(){
        recyclerView.setVisibility(View.VISIBLE);
        createFolderButton.setVisibility(View.INVISIBLE);
        createFolderButtonCancel.setVisibility(View.INVISIBLE);
        createFolderName.setVisibility(View.INVISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        db.collection("Users").document(current_user.getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String college_Name=documentSnapshot.get("College_Name").toString();
                String semester=documentSnapshot.get("Semester").toString();
                String stream=documentSnapshot.get("Stream").toString();
                if(documentSnapshot.get("Is_Ambassador").toString().equalsIgnoreCase("YES")){
                    createFolder.setVisibility(View.VISIBLE);
                }
                db.collection("Team/College_Ambassador/"+collegeName+"/"+semester+"/"+stream+"/Folder/Subjects").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        stringList=new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                            stringList.add(queryDocumentSnapshot.get("Folder_Name").toString());
                        }
                        if(stringList!=null) {
                            recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
                            recyclerViewAdapterSubjectFolders = new RecyclerViewAdapterSubjectFolders(MainActivity.this, stringList);
                            recyclerView.setAdapter(recyclerViewAdapterSubjectFolders);
                        }
                    }
                });
            }
        });
    }

}