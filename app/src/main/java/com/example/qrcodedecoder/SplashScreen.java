package com.example.qrcodedecoder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    TextView txt;
    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() !=null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_splash_screen);

        logo = findViewById(R.id.logo);
        txt = findViewById(R.id.txt);

        Animation animImg = AnimationUtils.loadAnimation(this, R.anim.splashanimimg);
        logo.startAnimation(animImg);
        Animation animTxt = AnimationUtils.loadAnimation(this, R.anim.splashanimtxt);
        txt.startAnimation(animTxt);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, GoogleSignIn.class);
                startActivity(intent);
                finish();
            }
        }, 3300);

    }
}