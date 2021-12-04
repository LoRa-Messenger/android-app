package com.example.loramessenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

public class ConnectToDeviceActivity extends AppCompatActivity implements BLEControllerListener{

    private ImageButton connectButton;
    private EditText inputDeviceName;
    private BLEController bleController;
    private String deviceAddress;
    private String deviceName;
    public static String name;
    private ProgressDialog progressDialog;
    private boolean isAlive = false;
    private Thread heartBeatThread = null;
    private RemoteControl remoteControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_device);

        this.bleController = BLEController.getInstance(this);
        this.remoteControl = new RemoteControl(this.bleController);

        inputDeviceName = findViewById(R.id.EnterDeviceName);
        initConnectButton();

        checkBLESupport();
        checkPermissions();
    }

    public void startHeartBeat() {
        this.isAlive = true;
        this.heartBeatThread = createHeartBeatThread();
        this.heartBeatThread.start();
    }

    public void stopHeartBeat() {
        if(this.isAlive) {
            this.isAlive = false;
            this.heartBeatThread.interrupt();
        }
    }

    private Thread createHeartBeatThread() {
        return new Thread() {
            @Override
            public void run() {
                while(ConnectToDeviceActivity.this.isAlive) {
                    heartBeat();
                    try {
                        Thread.sleep(1000l);
                    }catch(InterruptedException ie) { return; }
                }
            }
        };
    }

    private void heartBeat() {
        this.remoteControl.heartbeat();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    42);
        }
    }

    private void checkBLESupport() {
        // Check if BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.deviceAddress = null;
        this.bleController = BLEController.getInstance(this);
        this.bleController.addBLEControllerListener(this);
    }


    private void initConnectButton() {
        this.connectButton = findViewById(R.id.connectButton);
        this.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = inputDeviceName.getText().toString();
                Log.d("[BLE]","Searching..."+name);
                setProgressDialog();
                checkPermission();
            }
        });
    }

    private void setProgressDialog(){
        progressDialog = new ProgressDialog(ConnectToDeviceActivity.this);
        progressDialog.setMessage("Connecting " + name +"...");
        progressDialog.show();
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );
    }

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("[BLE]", "Initializing...");
            this.bleController.init();
        }else{
            Toast.makeText(this, "Require Permission!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void BLEControllerConnected() {
        Log.i("[BLE]","Connected");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
        startHeartBeat();
        startActivity(new Intent(ConnectToDeviceActivity.this, MainActivity.class));
    }

    @Override
    public void BLEControllerDisconnected() {

    }

    @Override
    public void BLEDeviceFound(String name, String address) {
        this.deviceAddress = address;
        this.connectButton.setEnabled(true);
        this.deviceName = name;

        bleController.connectToDevice(deviceAddress);
    }
}