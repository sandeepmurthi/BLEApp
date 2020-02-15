package com.example.bleapp;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.github.anastr.speedviewlib.Speedometer;
import pl.pawelkleczkowski.customgauge.CustomGauge;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.appcompat.app.AppCompatActivity;

import de.nitri.gauge.Gauge;


public class guage extends AppCompatActivity {
    private CustomGauge gauge1;
    private CustomGauge gauge3;
    Gauge guage;

    int gv;
    int i;
    private TextView text1;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.speedview);
        gauge1 = findViewById(R.id.gauge2);
        gauge3 = findViewById(R.id.gauge3);
        text1  = findViewById(R.id.textView1);
        text1.setText(String.valueOf(gauge1.getValue()));
        Button button1 = (Button) findViewById(R.id.decrease);
        Button button2 = (Button) findViewById(R.id.Increase);
        guage = (Gauge) findViewById(R.id.gauge1);
        gv=50;
        guage.setValue(gv);
        gauge1.setEndValue(100);
        gauge3.setEndValue(100);
        gauge1.setValue(50);
        gauge3.setValue(50);
        text1.setText(String.valueOf(gauge1.getValue()));

         button1.setOnClickListener( new View.OnClickListener(){
             public void onClick (View v){
                 if(gv>0) {
                     gv = gv - 10;
                     String val = Integer.toString(gv);
                     guage.moveToValue(gv);

                     gauge1.setValue(gv);
                     gauge3.setValue(gv);
                     text1.setText(String.valueOf(gauge1.getValue()));
                     guage.setUpperText(val);
                 }


            }
        });
        button2.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                if (gv<100) {
                    gv = gv + 10;
                    String val = Integer.toString(gv);
                    guage.moveToValue(gv);
                    gauge1.setValue(gv);
                    gauge3.setValue(gv);
                    text1.setText(String.valueOf(gauge1.getValue()));
                    guage.setUpperText(val);
                }

            }
        });

    }

}
