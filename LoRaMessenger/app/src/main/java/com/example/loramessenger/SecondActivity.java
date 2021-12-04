package com.example.loramessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;

public class SecondActivity extends AppCompatActivity {

    private BLEController bleController;
    public byte[] data;
    public TextView readData;
    public TextView writeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

//        this.bleController.read_write_Data(data);
//
//        readData = findViewById(R.id.readData);
//        writeData = findViewById(R.id.writeData);
//
//        String rData = new String(data, StandardCharsets.UTF_8);    //converting byte data to string to set it as text in the app
//
//        readData.setText(rData);



    }
}