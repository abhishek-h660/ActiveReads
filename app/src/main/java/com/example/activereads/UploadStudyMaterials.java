package com.example.activereads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.activereads.Adapter.RecyclerViewAdapterLibrary;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadStudyMaterials extends AppCompatActivity {

    FirebaseUser currentUser;
    FirebaseFirestore db;
    Button choose;
    String subject;
    EditText set_file_name;
    Spinner book_category;
    TextView toolbar_title;
    ProgressBar progressBar;
    FloatingActionButton upload;
    RecyclerView recyclerView;
    RecyclerViewAdapterLibrary recyclerViewAdapterLibrary;
    List<LibraryItems> libraryItemsList;
    ArrayAdapter<CharSequence> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_study_materials);
        initialise();
        setAvailable();


        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(set_file_name.getText().toString().isEmpty()){
                    Toast.makeText(UploadStudyMaterials.this, "Set File Name First", Toast.LENGTH_LONG).show();
                }else{
                    Intent browseIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    browseIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    browseIntent.setType("application/pdf");
                    startActivityForResult(browseIntent.createChooser(browseIntent, "Select File"), 121);
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.INVISIBLE);
                choose.setVisibility(View.VISIBLE);
                set_file_name.setVisibility(View.VISIBLE);
                book_category.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initialise(){
        Intent intent=getIntent();
        upload=findViewById(R.id.upload);
        recyclerView=findViewById(R.id.recyclerViewUploadStudyMaterials);
        toolbar_title=findViewById(R.id.toolbar_title);
        subject=intent.getStringExtra("Subject_Name");
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        db=FirebaseFirestore.getInstance();
        set_file_name=findViewById(R.id.set_file_name);
        choose=findViewById(R.id.choose);
        book_category=findViewById(R.id.category);
        progressBar=findViewById(R.id.uploadProgress);
        arrayAdapter=ArrayAdapter.createFromResource(this, R.array.itemCategory, android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        choose.setEnabled(false);
        progressBar.getProgressDrawable();
        progressBar.setVisibility(View.VISIBLE);
        final Uri uri=data.getData();
        String uriString=uri.toString();
        Log.d("DB_Am", uriString);
        File myFile=new File(uriString);
        final String name=set_file_name.getText().toString();

        db.collection("Users").document(currentUser.getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                StorageReference ref= FirebaseStorage.getInstance().getReference();
                StorageReference pdfReference=ref.child(documentSnapshot.get("College_Name").toString()+"/"+documentSnapshot.get("Semester").toString()+"/"+documentSnapshot.get("Stream"));
                //StorageReference pdfReference= reference.child(book_category.getText().toString());
                final StorageReference fileReference=pdfReference.child(name);
                fileReference.putFile(uri).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadStudyMaterials.this, "Uploading Failed"+e.toString(), Toast.LENGTH_LONG).show();
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map<String, String> data=new HashMap<>();
                                data.put("Link", uri.toString());
                                data.put("Name", name);
                                data.put("Category", book_category.getSelectedItem().toString());
                                db.collection("Team/College_Ambassador/"+documentSnapshot.get("College_Name")+"/"+documentSnapshot.get("Semester")+"/"+documentSnapshot.get("Stream")+"/Subjects/"+toolbar_title.getText().toString()).add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(UploadStudyMaterials.this, "Uploaded", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.INVISIBLE);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(UploadStudyMaterials.this, "Failed:"+e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void setAvailable(){
        toolbar_title.setText(subject);
        book_category.setAdapter(arrayAdapter);
        db.collection("Users").document(currentUser.getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.get("Is_Ambassador").toString().equalsIgnoreCase("YES")){
                    upload.setVisibility(View.VISIBLE);
                }
            }
        });

        final Context context=this;
        db.collection("Users").document(currentUser.getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String collegeName=documentSnapshot.get("College_Name").toString();
                String semester=documentSnapshot.get("Semester").toString();
                String stream=documentSnapshot.get("Stream").toString();
                db.collection("Team/College_Ambassador/"+collegeName+"/"+semester+"/"+stream+"/Subjects/"+subject).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        libraryItemsList=new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                            LibraryItems items=new LibraryItems();
                            items.setLink(queryDocumentSnapshot.get("Link").toString());
                            items.setBookName(queryDocumentSnapshot.get("Name").toString());
                            items.setCategory(queryDocumentSnapshot.get("Category").toString());
                            libraryItemsList.add(items);
                        }if(libraryItemsList!=null) {
                            recyclerView.setLayoutManager(new LinearLayoutManager(context));
                            recyclerViewAdapterLibrary = new RecyclerViewAdapterLibrary(context, libraryItemsList);
                            recyclerView.setAdapter(recyclerViewAdapterLibrary);
                        }else {
                            Toast.makeText(UploadStudyMaterials.this, "Null", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}