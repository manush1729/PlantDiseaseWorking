package com.example.newsmartindiahackthon;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;

public class SearchResult extends AppCompatActivity {
    String url = "https://www.google.com/search?q=";
    WebView webView;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        Bundle b=getIntent().getExtras();
        url+=b.getString("query");
        webView = (WebView)findViewById(R.id.web);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }
}