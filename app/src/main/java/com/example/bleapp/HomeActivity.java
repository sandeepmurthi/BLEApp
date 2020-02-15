package com.example.bleapp;

import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.view.MenuItem;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.bleapp.OperationActivity.bleDevice;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    public static final String KEY_DATA = "key_data";

    private ProgressDialog progressDialog;

    private EditText et_name, et_mac, et_uuid;
    private Switch sw_auto;
    private ImageView img_loading;
    private Animation operatingAnim;

    private LinearLayout layout_setting;
    private TextView txt_setting;
    //    UUID uuid="0000FFE1-1000-8000-00805F9B34FB";
    //    Mac mac="7C:01:0A:64:45:3E";
    //    String name=fitness;
    private DeviceAdapter mDeviceAdapter;
    private Button btn_scan,email, button;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();

        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);

        //when the user presses logout button
        //calling the logout method
        findViewById(R.id.buttonLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                SharedPrefManager.getInstance(getApplicationContext()).logout();
            }
        });
        Button startBtn = (Button) findViewById(R.id.Guage);
        startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(), guage.class));

            }
        });
        final Button button = (Button) findViewById(R.id.btnShow);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {



                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(HomeActivity.this, button);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.pop, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(HomeActivity.this,"You Have Chosen to feedback via : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        switch (item.getItemId()) {
                            case R.id.gmail:
                                sendEmail();
                                // do your code
                                return true;
                            case R.id.whatsapp:
                                whatsapp();
                                // do your code
                                return true;
                            case R.id.none:
                                // do your code
                                return true;

                            default:
                                return false;
                        }

                    }
                });

                popup.show();//showing popup menu
            }
        });//closing the setOnClickListener method



    }

    private void init() {
        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_scan.setText(getString(R.string.start_scan));
        btn_scan.setOnClickListener(this);

        et_name = (EditText) findViewById(R.id.et_name);
        et_mac = (EditText) findViewById(R.id.et_mac);
        et_uuid = (EditText) findViewById(R.id.et_uuid);
        sw_auto = (Switch) findViewById(R.id.sw_auto);

        layout_setting = (LinearLayout) findViewById(R.id.layout_setting);
        txt_setting = (TextView) findViewById(R.id.txt_setting);
        txt_setting.setOnClickListener(this);
        layout_setting.setVisibility(View.GONE);
        txt_setting.setText(getString(R.string.expand_search_settings));

        img_loading = (ImageView) findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        progressDialog = new ProgressDialog(this);

        mDeviceAdapter = new DeviceAdapter(this);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);
                }
            }

            @Override
            public void onDisConnect(final BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }

            @Override
            public void onDetail(BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    Intent intent = new Intent(HomeActivity.this, OperationActivity.class);
                    intent.putExtra(OperationActivity.KEY_DATA, bleDevice);
                    startActivity(intent);
                }
            }
        });
        ListView listView_device = (ListView) findViewById(R.id.list_device);
        listView_device.setAdapter(mDeviceAdapter);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                if (btn_scan.getText().equals(getString(R.string.start_scan))) {
                    checkPermissions();
                } else if (btn_scan.getText().equals(getString(R.string.stop_scan))) {
                    BleManager.getInstance().cancelScan();
                }
                break;

            case R.id.txt_setting:
                if (layout_setting.getVisibility() == View.VISIBLE) {
                    layout_setting.setVisibility(View.GONE);
                    txt_setting.setText(getString(R.string.expand_search_settings));
                } else {
                    layout_setting.setVisibility(View.VISIBLE);
                    txt_setting.setText(getString(R.string.retrieve_search_settings));
                }
                break;
        }
    }
    protected void whatsapp()
    {
        User user = SharedPrefManager.getInstance(this).getUser();

        //setting the values to the textviews
        String id = String.valueOf(user.getId());
        String username = user.getUsername();
        String emailid = user.getEmail();
        String headerReceiver = "Feed back From Uses:-\n";// Replace with your message.
        String bodyMessageFormal = "Type Your message here to reach us\n";// Replace with your message.
        String whatsappContain = headerReceiver +"ID:-"+ id +"\nUsername:-"+ username +"\nemailID:-"+ emailid +"\n"+ bodyMessageFormal;
        String trimToNumner = "+919008675956"; //10 digit number
        //String trimToNumner = "+919000125121"; //10 digit number
        Intent intent = new Intent ( Intent.ACTION_VIEW );
        intent.setData ( Uri.parse ( "https://wa.me/" + trimToNumner + "/?text=" + whatsappContain ) );
        startActivity ( intent );




    }

    protected void sendEmail() {

        Log.i("Send email", "");
        User user = SharedPrefManager.getInstance(this).getUser();

        //setting the values to the textviews
        String id = String.valueOf(user.getId());
        String username = user.getUsername();
        String emailid = user.getEmail();
        String headerReceiver = "Feed back From Uses:-\n";// Replace with your message.
        String bodyMessageFormal = "Type Your message here to reach us\n";// Replace with your message.
        String whatsappContain = headerReceiver +"ID:-"+ id +"\nUsername:-"+ username +"\nemailID:-"+ emailid +"\n"+ bodyMessageFormal;

        String[] TO = {"3dprinting.teeaa@gmail.com "};
        String[] CC = {"sandeepmurthi@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Customer Feedback from PatrIOT APP");
        emailIntent.putExtra(Intent.EXTRA_TEXT, whatsappContain+"\n"+"Thanks for feedback,Please write your suggestions here..");
        emailIntent.setType("message/rfc822");

        try {

            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            //Log.i("Finished sending email...", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(HomeActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    setScanRule();
                    startScan();
                }
                break;
        }
    }

    private void setScanRule() {
        String[] uuids;
        String str_uuid = et_uuid.getText().toString();
        if (TextUtils.isEmpty(str_uuid)) {
            uuids = null;
        } else {
            uuids = str_uuid.split(",");
        }
        UUID[] serviceUuids = null;
        if (uuids != null && uuids.length > 0) {
            serviceUuids = new UUID[uuids.length];
            for (int i = 0; i < uuids.length; i++) {
                String name = uuids[i];
                String[] components = name.split("-");
                if (components.length != 5) {
                    serviceUuids[i] = null;
                } else {
                    serviceUuids[i] = UUID.fromString(uuids[i]);
                }
            }
        }

        String[] names;
        String str_name = et_name.getText().toString();
        if (TextUtils.isEmpty(str_name)) {
            names = null;
        } else {
            names = str_name.split(",");
        }

        String mac = et_mac.getText().toString();

//        boolean isAutoConnect = sw_auto.isChecked();
        boolean isAutoConnect=true;

        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(serviceUuids)
                .setDeviceName(true, names)
                .setDeviceMac(mac)
                .setAutoConnect(isAutoConnect)
                .setScanTimeOut(10000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);
                btn_scan.setText(getString(R.string.stop_scan));
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                if (bleDevice.getMac().equals(bleDevice.getMac())){
                    mDeviceAdapter.clearScanDevice();
                    mDeviceAdapter.notifyDataSetChanged();
                    BleManager.getInstance().cancelScan();
                }
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
//                Log.e(TAG,scanResultList.toString());
                for (BleDevice bleDevice:scanResultList){
                    Log.e(TAG,bleDevice.getMac());
                    if(bleDevice.getMac().equals(bleDevice.getMac())){
                        mDeviceAdapter.addDevice(bleDevice);
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void connect(final BleDevice bleDevice){
//        String mac="7C:01:0A:64:45:3E";
        String mac=bleDevice.getMac();
        BleManager.getInstance().connect(mac, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Connecting to PatrIOT");
                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                btn_scan.setText(getString(R.string.start_scan));
                progressDialog.dismiss();
                Toast.makeText(HomeActivity.this, getString(R.string.connect_fail), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                Log.e(TAG,bleDevice.toString());
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();
//
                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                if (isActiveDisConnected) {
                    Toast.makeText(HomeActivity.this, getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(HomeActivity.this, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
//                    ObserverManager.getInstance().notifyObserver(bleDevice);
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().disconnect(bleDevice);
        }
    }

}
