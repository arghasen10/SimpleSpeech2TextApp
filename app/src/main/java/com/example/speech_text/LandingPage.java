package com.example.speech_text;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class LandingPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        SharedPreferences sp1=this.getSharedPreferences("Login", MODE_PRIVATE);
        String unm=sp1.getString("Unm", null);
        if (unm != null){
            // User is signed in
            // Start home activity
            startActivity(new Intent(LandingPage.this, MainActivity.class));
        } else {
            // No user is signed in
            // start login activity
            startActivity(new Intent(LandingPage.this, LoginActivity.class));
        }
        finish();
    }
}