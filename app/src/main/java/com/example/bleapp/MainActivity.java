package com.example.bleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    ImageView iv_splash;
    private Handler handler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv_splash=findViewById(R.id.iv_splash);

        Picasso.with(MainActivity.this).load(R.mipmap.splashscreen).placeholder(R.mipmap.splashscreen)
                .error(R.mipmap.splashscreen)
                //.resize(256, 256)
                //.centerCrop()
                .into(iv_splash);

        handler.postDelayed(th,3000);
    }
    Runnable th=new Runnable(){

        @Override
        public void run() {

            Intent intent = new Intent(MainActivity.this, login_activity.class);
            startActivity(intent);
            finish();
        }
    };
}
