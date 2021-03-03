package com.example.activereads;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.URLEncoder;

public class ViewIntent extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore;
    WebView webView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_intent);
        firebaseFirestore=FirebaseFirestore.getInstance();
        StorageReference ref= FirebaseStorage.getInstance().getReference();
        webView=findViewById(R.id.webView);
        progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.getProgressDrawable();

        try {
            Intent intent=getIntent();
            String pdf=intent.getStringExtra("Link");
            String url="";
            try {
                url= URLEncoder.encode(pdf,"UTF-8");
            }catch (Exception e){
                Log.d("DB_Am", e.toString());
            }
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);
            webView.setWebChromeClient(new WebChromeClient());
            webView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    webView.loadUrl("javascript:(function(){"+"document.querySelector('[role=\"toolbar\"]').remove();})()");
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
            webView.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url="+url);

        } catch (Exception e) {
            Log.d("DB_Am", "Error:"+e.toString());
        }
    }
}