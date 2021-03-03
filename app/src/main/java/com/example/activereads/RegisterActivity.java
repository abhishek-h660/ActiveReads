package com.example.activereads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.activereads.MainActivity;
import com.example.activereads.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    //EditText Stuff
    EditText password;
    EditText email;
    Spinner collegeName;
    Spinner stream;
    Spinner semester;
    EditText phone;

    //Button stuff
    Button sign_in;
    Button verified;

    //Firebase stuffs
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore firebaseFirestore;
    public String stringPassword;
    Map<String, String> hashData;
    TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toolbarTitle=findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Register");
        initialise();

        ArrayAdapter<CharSequence> adapterSemester=ArrayAdapter.createFromResource(this, R.array.Semester, android.R.layout.simple_spinner_dropdown_item);
        adapterSemester.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        semester.setAdapter(adapterSemester);

        ArrayAdapter<CharSequence> adapterCollegeList=ArrayAdapter.createFromResource(this, R.array.CollegeList, android.R.layout.simple_spinner_dropdown_item);
        adapterSemester.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        collegeName.setAdapter(adapterCollegeList);

        ArrayAdapter<CharSequence> adapterStreamList=ArrayAdapter.createFromResource(this, R.array.BranchList, android.R.layout.simple_spinner_dropdown_item);
        adapterSemester.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stream.setAdapter(adapterStreamList);

        Context context=RegisterActivity.this;
        final SharedPreferences sharedPreferences=RegisterActivity.this.getPreferences(context.MODE_PRIVATE);
        currentUser=mAuth.getCurrentUser();

        /*firebaseFirestore.collection("CollegeList").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                collegeList=new ArrayList<>();
                for(DocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                    collegeList.add(documentSnapshot.get("Name").toString());
                }
                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(RegisterActivity.this,android.R.layout.simple_spinner_dropdown_item, collegeList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                collegeName.setAdapter(arrayAdapter);
                if(currentUser!=null){
                    collegeName.setSelection(collegeList.indexOf(sharedPreferences.getString("College_Name", "")));
                }
            }
        });*/

        if(currentUser!=null){
            email.setText(sharedPreferences.getString("User_Name", ""));
            collegeName.setSelection(Integer.parseInt(sharedPreferences.getString("College_Name", "")));
            stream.setSelection(Integer.parseInt(sharedPreferences.getString("Stream", "")));
            semester.setSelection(Integer.parseInt(sharedPreferences.getString("Stream", "")));
            phone.setText(sharedPreferences.getString("User_Phone", ""));
        }

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentUser=mAuth.getCurrentUser();
                if(currentUser==null) {
                    stringPassword = password.getText().toString();
                    if (!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty() && !phone.getText().toString().isEmpty() && !semester.getSelectedItem().toString().equalsIgnoreCase("Select Semester") && !collegeName.getSelectedItem().toString().equalsIgnoreCase("Select College") && !stream.getSelectedItem().toString().equalsIgnoreCase("Select Stream")) {
                        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(RegisterActivity.this, "Sign Up Successful", Toast.LENGTH_LONG).show();
                                currentUser = mAuth.getCurrentUser();
                                hashData.put("User_Name", currentUser.getEmail());
                                hashData.put("User_Phone", phone.getText().toString());
                                hashData.put("College_Name", collegeName.getSelectedItem().toString());
                                hashData.put("Stream", stream.getSelectedItem().toString());
                                hashData.put("Semester", semester.getSelectedItem().toString());
                                hashData.put("has_paid","NO");
                                hashData.put("Is_Ambassador", "NO");
                                SharedPreferences.Editor editor=sharedPreferences.edit();
                                editor.putString("User_Name", currentUser.getEmail());
                                editor.putString("User_Phone", phone.getText().toString());
                                editor.putString("College_Name", String.valueOf(collegeName.getSelectedItemPosition()));
                                editor.putString("Stream", String.valueOf(stream.getSelectedItemPosition()));
                                editor.putString("Semester", String.valueOf(semester.getSelectedItemPosition()));
                                editor.apply();
                                sign_in.setVisibility(View.INVISIBLE);
                                currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(RegisterActivity.this, "Verification mail has been sent", Toast.LENGTH_LONG).show();
                                        verified.setVisibility(View.VISIBLE);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegisterActivity.this, "Enter Valid Email", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RegisterActivity.this, "SignUp Failed:" + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Fill all the fields !!", Toast.LENGTH_LONG).show();
                    }
                }else{
                    firebaseFirestore.collection("Users").whereEqualTo("User_Name", currentUser.getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for(DocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                                verified.setVisibility(View.INVISIBLE);
                                Toast.makeText(RegisterActivity.this, "Already Registered..Press Home to get Out of App.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty() && !phone.getText().toString().isEmpty() && !semester.getSelectedItem().toString().equalsIgnoreCase("Select Semester") && !collegeName.getSelectedItem().toString().equalsIgnoreCase("Select College") && !stream.getSelectedItem().toString().equalsIgnoreCase("Select Stream")) {
                                stringPassword=password.getText().toString();
                                hashData.put("User_Name", email.getText().toString());
                                hashData.put("College_Name", collegeName.getSelectedItem().toString());
                                hashData.put("Branch_Name", stream.getSelectedItem().toString());
                                hashData.put("Semester", semester.getSelectedItem().toString());
                                hashData.put("User_Phone", phone.getText().toString());
                                hashData.put("has_paid", "NO");
                                hashData.put("Is_Ambassador", "NO");
                                hashData.put("has_profile_pic", "NO");
                                if(!currentUser.getEmail().toString().equalsIgnoreCase(email.getText().toString())) {
                                    currentUser.updateEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(RegisterActivity.this, "Verification Email has been sent to Your email", Toast.LENGTH_LONG).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(RegisterActivity.this, "Recheck Your Entry", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    });
                                }else{
                                    currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(RegisterActivity.this, "Verification Email has been sent to Your email", Toast.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(RegisterActivity.this, "Recheck Your Entry", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                sign_in.setVisibility(View.INVISIBLE);
                                verified.setVisibility(View.VISIBLE);
                            }else{
                                Toast.makeText(RegisterActivity.this, "Fill All fields", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        verified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signInWithEmailAndPassword(currentUser.getEmail(),stringPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Task<Void> voidTask=mAuth.getCurrentUser().reload();
                        voidTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                currentUser=mAuth.getCurrentUser();
                                if(currentUser.isEmailVerified()){
                                    Toast.makeText(RegisterActivity.this, "Signed IN", Toast.LENGTH_LONG).show();
                                    Log.d("DB_Email", "Email Verified");
                                    verified.setVisibility(View.INVISIBLE);
                                    firebaseFirestore.collection("Users").document(currentUser.getEmail()).set(hashData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(RegisterActivity.this, "Successful", Toast.LENGTH_LONG).show();
                                            Intent intent=new Intent(RegisterActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(RegisterActivity.this, "Failed:"+e.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }else{
                                    Toast.makeText(RegisterActivity.this, "Verify Account from Your Email then click on CREATE PROFILE", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Verify Account from Your Email", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
    }

    private void initialise(){
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        collegeName=findViewById(R.id.college);
        stream=findViewById(R.id.stream);
        semester=findViewById(R.id.semester);
        phone=findViewById(R.id.phone);

        sign_in=findViewById(R.id.sign_up);
        verified=findViewById(R.id.create_profile);

        hashData=new HashMap<>();

        verified.setVisibility(View.INVISIBLE);

        // Creating instance of FirebaseAuth
        mAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
    }

    @Override
    public void onBackPressed() {
        if(currentUser!=null)
        firebaseFirestore.collection("Users").whereEqualTo("User_Name", currentUser.getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    finish();
                    return;
                }
                Toast.makeText(RegisterActivity.this, "Please Verify Email and Create Profile", Toast.LENGTH_LONG).show();
            }
        });
    }
}