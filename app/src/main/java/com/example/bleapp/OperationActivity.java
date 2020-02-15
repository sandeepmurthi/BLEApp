package com.example.bleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

public class OperationActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String KEY_DATA = "key_data";
    private static final String TAG = "OperationActivity";
    public static BleDevice bleDevice;
    private String[] titles = new String[3];
    TextView text;
    Button read_btn,write_btn,notify_btn,stopnotify;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);

        text=findViewById(R.id.text);
//        read_btn=findViewById(R.id.read_btn);
        write_btn=findViewById(R.id.write_btn);
        notify_btn=findViewById(R.id.notify_btn);
        stopnotify=findViewById(R.id.stopnotify);


//        read_btn.setOnClickListener(this);
        write_btn.setOnClickListener(this);
        notify_btn.setOnClickListener(this);
        stopnotify.setOnClickListener(this);

        initData();

        Log.e("Data",bleDevice.getMac());
    }
    private void initData() {
        bleDevice = getIntent().getParcelableExtra(KEY_DATA);
        if (bleDevice == null)
            finish();

    }

    @Override
    public void onClick(View view) {

        text.setText("");
        switch (view.getId()){
//            case R.id.read_btn:
//                if (bleDevice!=null){
//                    BleManager.getInstance().read(
//                            bleDevice,
//                            "0000FFE0-0000-1000-8000-00805F9B34FB",
//                            "0000FFE1-0000-1000-8000-00805F9B34FB",
//                            new BleReadCallback() {
//                                @Override
//                                public void onReadSuccess(byte[] data) {
//                                    String str = new String(data);
//                                    Log.e(TAG, "onReadSuccess: "+ str);
//                                    text.setText("onReadSuccess: "+ str);
//                                    Toast.makeText(getApplicationContext(),"onReadSuccess: "+ str,Toast.LENGTH_SHORT).show();
//                                }
//
//                                @Override
//                                public void onReadFailure(BleException exception) {
//                                    Log.e(TAG, "onReadFailure: " );
//                                    text.setText("onReadFailure: "+ exception.getDescription());
//                                    Toast.makeText(getApplicationContext(),"onReadFailure: "+ exception.getDescription(),Toast.LENGTH_SHORT).show();
//
//                                }
//                            });
//
//                }
//                break;
            case R.id.write_btn:
                if(bleDevice!=null){
                    if (write_btn.getText().equals(getString(R.string.ON))) {
                        write_btn.setText(R.string.OFF);
                        //EditText text_input=findViewById(R.id.text_input);
                        //String textStr=text_input.getText().toString();
                        String textStr="1";
                        BleManager.getInstance().write(
                                bleDevice,
                                "0000FFE0-0000-1000-8000-00805F9B34FB",
                                "0000FFE1-0000-1000-8000-00805F9B34FB",
                                textStr.getBytes(),
                                new BleWriteCallback() {
                                    @Override
                                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                        String s = new String(justWrite);
                                        Log.e(TAG, "WriteSuccess: ");
                                        text.setText("Successfully send "+s);
                                        Toast.makeText(getApplicationContext(),"Successfully Turned ON :"+s,Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onWriteFailure(BleException exception) {
                                        Log.e(TAG, "WriteFailure: "+exception.getDescription() );
                                        text.setText("Failed to send");
                                        Toast.makeText(getApplicationContext(),"Failed to send "+exception.getDescription(),Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                    else if (write_btn.getText().equals(getString(R.string.OFF)))
                    {
                        write_btn.setText(R.string.ON);
                        //EditText text_input=findViewById(R.id.text_input);
                        //String textStr=text_input.getText().toString();
                        String textStr="0";
                        BleManager.getInstance().write(
                                bleDevice,
                                "0000FFE0-0000-1000-8000-00805F9B34FB",
                                "0000FFE1-0000-1000-8000-00805F9B34FB",
                                textStr.getBytes(),
                                new BleWriteCallback() {
                                    @Override
                                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                        String s = new String(justWrite);
                                        Log.e(TAG, "WriteSuccess: ");
                                        text.setText("Successfully send "+s);
                                        Toast.makeText(getApplicationContext(),"Successfully Turned ON :"+s,Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onWriteFailure(BleException exception) {
                                        Log.e(TAG, "WriteFailure: "+exception.getDescription() );
                                        text.setText("Failed to send");
                                        Toast.makeText(getApplicationContext(),"Failed to send "+exception.getDescription(),Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }

                }
                break;
            case R.id.notify_btn:
                if (bleDevice!=null){
                    Log.e(TAG, "onClick: notify_btn" );
                    BleManager.getInstance().notify(
                            bleDevice,
                            "0000FFE0-0000-1000-8000-00805F9B34FB",
                            "0000FFE1-0000-1000-8000-00805F9B34FB",
                            new BleNotifyCallback() {
                                @Override
                                public void onNotifySuccess() {
                                    Toast.makeText(OperationActivity.this,"onNotifySuccess",Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "onNotifySuccess: " );
                                }

                                @Override
                                public void onNotifyFailure(BleException exception) {
                                    Toast.makeText(OperationActivity.this,"onNotifyFailure "+exception.getDescription(),Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "onNotifyFailure: " );
                                }

                                @Override
                                public void onCharacteristicChanged(byte[] data) {
                                    String s = new String(data);
                                    Toast.makeText(OperationActivity.this,"'"+s+"'",Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "onCharacteristicChanged: "+s);
                                    text.setText("Values: "+s);
                                }
                            });
                }
                break;
            case R.id.stopnotify:
                if (bleDevice!=null){
                    BleManager.getInstance().stopNotify(bleDevice,"0000FFE0-0000-1000-8000-00805F9B34FB","0000FFE1-0000-1000-8000-00805F9B34FB");
                    Log.e(TAG, "onClick: stopNotify" );
                }
                break;
        }
    }
}
