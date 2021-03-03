package com.example.activereads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {
    ProgressBar progressBar;
    EditText editName;
    Spinner editCollege;
    Spinner editStream;
    Spinner editYear;
    EditText editPhone;
    TextView toolbarTitle;
    Button saveChange;
    ImageView captureProfile;
    FirebaseFirestore db;
    StorageReference storageReference;
    Map<String, Object> hashData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        editPhone=findViewById(R.id.editPhone);
        initialize();
        setAvailable();
        toolbarTitle.setText("Profile Details");
        captureProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 3);
                }else {
                    profilePictureStuffs();
                }
            }
        });

        saveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.getProgressDrawable();
                if(!editPhone.getText().toString().isEmpty() && !editCollege.getSelectedItem().toString().equalsIgnoreCase("Select College")&& !editName.getText().toString().isEmpty() &&
                        !editStream.getSelectedItem().toString().equalsIgnoreCase("Select Stream") && !editYear.getSelectedItem().toString().equalsIgnoreCase("Select Semester")){
                    db.collection("Users").whereEqualTo("User_Name", FirebaseAuth.getInstance().getCurrentUser().getEmail())
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Map<String, Object> hashMap=getHashData();
                            for(QueryDocumentSnapshot documentSnapshot:task.getResult()){
                                DocumentReference documentReference=db.collection("Users").document(documentSnapshot.getId());
                                documentReference.update(hashMap);
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(EditProfile.this, "Changes Saved", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(EditProfile.this, "Fill all Fields", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initialize(){
        progressBar=findViewById(R.id.progressBar2);
        editName=findViewById(R.id.First_Name);
        editCollege=findViewById(R.id.editCollage);
        editStream=findViewById(R.id.editBranch);
        editYear=findViewById(R.id.editYear);
        toolbarTitle=findViewById(R.id.toolbar_title);
        saveChange=findViewById(R.id.saveChange);
        captureProfile=findViewById(R.id.takePic);

        db=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getEmail()).child("Profile_Picture");
    }

    private void setAvailable(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.getProgressDrawable();
        final ArrayAdapter<CharSequence> adapterSemester=ArrayAdapter.createFromResource(this, R.array.Semester, android.R.layout.simple_spinner_dropdown_item);
        adapterSemester.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editYear.setAdapter(adapterSemester);

        final ArrayAdapter<CharSequence> adapterCollegeList=ArrayAdapter.createFromResource(this, R.array.CollegeList, android.R.layout.simple_spinner_dropdown_item);
        adapterSemester.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editCollege.setAdapter(adapterCollegeList);

        final ArrayAdapter<CharSequence> adapterStreamList=ArrayAdapter.createFromResource(this, R.array.BranchList, android.R.layout.simple_spinner_dropdown_item);
        adapterSemester.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editStream.setAdapter(adapterStreamList);

        db.collection("Users").whereEqualTo("User_Name", FirebaseAuth.getInstance().getCurrentUser().getEmail()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot documentSnapshot:task.getResult()){
                            if(documentSnapshot.contains("Profile_Picture")){
                                storageReference.getBytes(2024l*2024l).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        captureProfile.setImageBitmap(bitmap);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditProfile.this, "Profile Picture Retrieve failed", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            if(documentSnapshot.contains("First_Name")){
                                editName.setText(documentSnapshot.get("First_Name").toString());
                            }
                            editCollege.setSelection(adapterCollegeList.getPosition(documentSnapshot.get("College_Name").toString()));
                            editStream.setSelection(adapterStreamList.getPosition(documentSnapshot.get("Stream").toString()));
                            editPhone.setText(documentSnapshot.get("User_Phone").toString());
                            editYear.setSelection(adapterSemester.getPosition(documentSnapshot.get("Semester").toString()));
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, "Unable to connect", Toast.LENGTH_LONG).show();
            }
        });
    }

    private Map<String, Object> getHashData(){
        hashData=new HashMap<>();
        hashData.put("First_Name", editName.getText().toString());
        hashData.put("College_Name", editCollege.getSelectedItem().toString());
        hashData.put("Stream", editStream.getSelectedItem().toString());
        hashData.put("User_Phone", editPhone.getText().toString());
        hashData.put("Semester", editYear.getSelectedItem().toString());
        return  hashData;
    }

    private void profilePictureStuffs(){
        PopupMenu popupMenu=new PopupMenu(EditProfile.this, captureProfile);
        popupMenu.getMenuInflater().inflate(R.menu.menu_profile, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.camera) {
                    Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(captureImageIntent, 1);
                } else if (menuItem.getItemId() == R.id.chooseFromGallery) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
                }

                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==3){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                profilePictureStuffs();
            }else{
                Toast.makeText(EditProfile.this, "Please Grant the Permission For Camera", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode== Activity.RESULT_OK){
            Bitmap bitmap=(Bitmap)data.getExtras().get("data");
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100, stream);
            byte [] bytesData=stream.toByteArray();
            storageReference.putBytes(bytesData);
            captureProfile.setImageBitmap(bitmap);
            db.collection("Users").whereEqualTo("User_Name",FirebaseAuth.getInstance().getCurrentUser().getEmail()).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for(QueryDocumentSnapshot documentSnapshot:task.getResult()){
                                DocumentReference documentReference=db.collection("Users").document(documentSnapshot.getId());
                                documentReference.update("Profile_Picture", "YES");
                            }
                        }
                    });
            Toast.makeText(EditProfile.this, "Profile Picture Saved", Toast.LENGTH_LONG).show();
        }else if(requestCode==2 && resultCode==Activity.RESULT_OK){
            try {
                InputStream inputStream = EditProfile.this.getContentResolver().openInputStream(data.getData());
                Bitmap bitmap=BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100, stream);
                byte [] bytesData=stream.toByteArray();
                storageReference.putBytes(bytesData);
                captureProfile.setImageBitmap(bitmap);
                db.collection("Users").whereEqualTo("User_Name",FirebaseAuth.getInstance().getCurrentUser().getEmail()).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for(QueryDocumentSnapshot documentSnapshot:task.getResult()){
                                    DocumentReference documentReference=db.collection("Users").document(documentSnapshot.getId());
                                    documentReference.update("Profile_Picture", "YES");
                                }
                                Toast.makeText(EditProfile.this, "Profile Picture Saved", Toast.LENGTH_LONG).show();
                            }
                        });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
}