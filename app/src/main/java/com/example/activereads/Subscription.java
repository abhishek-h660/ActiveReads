package com.example.activereads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class Subscription extends AppCompatActivity implements PaymentResultListener{
    TextView toolbarTitle;
    Button start_now;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    String payment;
    Double total;
    TextView textView9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        toolbarTitle=findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Subscription");
        start_now=findViewById(R.id.start_now);
        start_now.setVisibility(View.INVISIBLE);
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        db=FirebaseFirestore.getInstance();
        textView9=findViewById(R.id.textView9);
        db.collection("Users").whereEqualTo("User_Name", currentUser.getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(QueryDocumentSnapshot documentSnapshot:task.getResult()){
                    if(documentSnapshot.get("has_paid").toString().equals("NO")){
                        db.collection("PaymentAmount").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (QueryDocumentSnapshot documentSnapshot1 : task.getResult()) {
                                    payment = documentSnapshot1.get("amount").toString();
                                    total = Double.parseDouble(payment);
                                    total *= 100;
                                    start_now.setVisibility(View.VISIBLE);
                                    textView9.setText(documentSnapshot1.get("text").toString());

                                }
                            }
                        });
                    }else{
                        String expiryDate=documentSnapshot.get("Expiry_Date").toString();
                        textView9.setText("Expiry Date : "+expiryDate);
                        start_now.setVisibility(View.VISIBLE);
                        start_now.setText("Subscribed");
                        start_now.setEnabled(false);
                    }
                }
            }
        });

        start_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPayment();
            }
        });
    }
    public void startPayment(){
        final Activity activity=this;
        final Checkout co=new Checkout();

        co.setKeyID(BuildConfig.RAZORPAY_API_KEY);

        try {
            db.collection("Users").whereEqualTo("User_Name", currentUser.getEmail().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for (final QueryDocumentSnapshot documentSnapshot:task.getResult()){
                        DocumentReference documentReference=db.collection("Users").document(documentSnapshot.getId());
                        try {
                            final JSONObject options=new JSONObject();
                            options.put("Name", "Active Group");
                            options.put("description", "App Payment");
                            options.put("currency", "INR");
                            try {
                                options.put("amount", total);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try{
                                final JSONObject preFill = new JSONObject();
                                preFill.put("email", currentUser.getEmail().toString());
                                preFill.put("contact", documentSnapshot.get("User_Phone"));
                                options.put("prefill", preFill);
                                co.open(activity, options);
                            }catch (Exception e){
                                Log.d("Payment_DB", e.toString());
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });

        }catch (Exception e){
            Log.d("Am", e.toString());
        }
    }
    @Override
    public void onPaymentSuccess(String s) {
        Toast.makeText(Subscription.this, s, Toast.LENGTH_LONG).show();
        Intent getInent=getIntent();
        String college_Name=getInent.getStringExtra("Collection_Path");
        db.collection("Users").whereEqualTo("User_Name", currentUser.getEmail().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot documentSnapshot:task.getResult()){
                    DocumentReference documentReference=db.collection("Users").document(documentSnapshot.getId());
                    documentReference.update("has_paid", "YES");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        String expiryDate=LocalDate.now().plusMonths(6).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        documentReference.update("Expiry_Date", expiryDate);
                        textView9.setText("Expiry Date"+expiryDate);
                        start_now.setText("Subscribed");
                        start_now.setEnabled(false);
                    }
                }
            }
        });
        Intent intent=new Intent(Subscription.this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(Subscription.this, s, Toast.LENGTH_LONG).show();
    }
}