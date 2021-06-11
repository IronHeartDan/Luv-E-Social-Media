package com.example.luve;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.white));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    intent = new Intent(splash.this, SignIn.class);
                } else {
                    intent = new Intent(splash.this, MainActivity.class);
                }
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();

            }
        }, 2000);
    }
}