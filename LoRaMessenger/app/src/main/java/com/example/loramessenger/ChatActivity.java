package com.example.loramessenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.loramessenger.adapter.ChatAdapter;
import com.example.loramessenger.model.ChatModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity implements ItemClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private RecyclerView recyclerView;
    private EditText messagebox;

    final ArrayList<ChatModel> chatModels = new ArrayList<>();
    int internalID = 1;
    private String recipientID = "0";
    private String contactName = "John";
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;
    private boolean mConnected = false;
    ChatAdapter chatAdapter = new ChatAdapter(chatModels, ChatActivity.this);
    Calendar mCalendar = Calendar.getInstance();
    private InputStream fis;
    private OutputStream fos;
    private Context context;
    private final int NUM_MESSAGE_LINE_FILE = 4;

//    BluetoothGattService mSender;           // Service for Sender
//    BluetoothGattService mReceiver;         // Service for Receiver
    private BluetoothGattCharacteristic sen_rec_ID;
    private BluetoothGattCharacteristic sen_mes_ID;
    private BluetoothGattCharacteristic sen_text;
    private BluetoothGattCharacteristic sen_processed;

    private BluetoothGattCharacteristic rec_sen_ID;
    private BluetoothGattCharacteristic rec_mes_ID;
    private BluetoothGattCharacteristic rec_time;
    private BluetoothGattCharacteristic rec_lat;
    private BluetoothGattCharacteristic rec_long;
    private BluetoothGattCharacteristic rec_text;
    private BluetoothGattCharacteristic rec_processed;


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            initCharacteristics(mBluetoothLeService.getSupportedGattServices());
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Extract the characteristics
                initCharacteristics(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_RECEIVED.equals(action)) {
                if(intent.getStringExtra(BluetoothLeService.UPDATE_CHAT).equals("true")){
                    receiveBLEMessage();
                }
            }
            else if (BluetoothLeService.ACTION_DATA_READ_AVAILABLE.equals(action)) {
                if(intent.getStringExtra(BluetoothLeService.UPDATE_CHAT).equals("true")){
                    if(rec_text.getValue() != null){
                        updateChatMessage();
                    }
                }
                mBluetoothLeService.completedCommand();
            }
        }
    };

    private void updateChatMessage() {
        ChatModel chatModel = new ChatModel();
        byte[] d = rec_sen_ID.getValue();
        int partnerID = 0;
        for (int i = 0; i < d.length; i++)
        {
            partnerID += ((int) d[i] & 0xffL) << (8 * i);
        }
        chatModel.setPartnerID(String.valueOf(partnerID));

        if(chatModel.getPartnerID().equals(recipientID)){
            internalID ++;
            //MessageID, for now use internal ID
            chatModel.setId(String.valueOf(internalID));
            //set isMe
            chatModel.setMe("f");

            //time
//            byte[] b = rec_time.getValue();
//            long time = 0;
//            for (int i = 0; i < b.length; i++)
//            {
//                time += ((long) b[i] & 0xffL) << (8 * i);
//            }
            //For now use Android time until time is fixed on embedded side
            mCalendar = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);
            String formattedDate = df.format(mCalendar.getTime());
            chatModel.setTime(formattedDate);

            //message
            byte[] a = rec_text.getValue();
            String test2 = new String(a);
            chatModel.setMessage(test2);
            chatModels.add(chatModel);
            messagebox.setText("");
            chatAdapter.notifyItemInserted(internalID);
            recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
        }
        else{
            Log.i(TAG, String.format("INFO: Received message from ID: %s while recipient ID is: %s", chatModel.getPartnerID(), recipientID));
        }

        //update characteristic so that board can receive more messages
        mBluetoothLeService.writeCharacteristic(rec_processed,"1");
    }

    private void receiveBLEMessage() {
        mBluetoothLeService.readCharacteristic(rec_sen_ID);
        mBluetoothLeService.readCharacteristic(rec_time);
        mBluetoothLeService.readCharacteristic(rec_text);
        mBluetoothLeService.readCharacteristic(rec_mes_ID);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        final Intent intent = getIntent();
        context = getApplicationContext();
        recipientID = intent.getStringExtra("RecipientID");
        contactName = intent.getStringExtra("ContactName");
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        setTitle(String.format("%s @ID: %s",contactName, recipientID));
        Button btnSend;

        recyclerView = findViewById(R.id.chat_list);
        messagebox = findViewById(R.id.et_chat_box);

        btnSend = findViewById(R.id.btn_chat_send);


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        recyclerView.setAdapter(chatAdapter);
        chatAdapter.setClickListener(this);


        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        //Check file for previous history and update view
        try {
            // open the file for reading

            fis = context.openFileInput(contactName);

            // if file the available for reading
            if (fis != null) {

                // prepare the file for reading
                InputStreamReader chapterReader = new InputStreamReader(fis);
                BufferedReader buffreader = new BufferedReader(chapterReader);

                String line;
                ChatModel chatModel = new ChatModel();
                int counterInternalMessages = 0;
                int counterMessages = 0;

                //make sure ID is still on line 1
                line = buffreader.readLine();
                if(!line.equals(recipientID)){
                    Log.e(TAG, String.format("ERROR: wrong read ID from file on line 1: %s was expecting: %s", line, recipientID));
                }

                // read every line of the file into the line-variable, on line at the time
                do {
                    line = buffreader.readLine();
                    counterInternalMessages++;
                    if(line != null){ //check to see if empty line is read
                        switch(counterInternalMessages){
                            case 1:
                                chatModel.setId(line);
                                break;
                            case 2:
                                chatModel.setTime(line);
                                break;
                            case 3:
                                chatModel.setMessage(line);
                                break;
                            default: //case 4
                                chatModel.setMe(line);
                        }
                        if(counterInternalMessages == NUM_MESSAGE_LINE_FILE ){
                            counterInternalMessages = 0;
                            chatModels.add(chatModel);
                            chatModel = new ChatModel(); //need this otherwise all loaded messages are the same
                            counterMessages++;
                        }
                    }
                } while (line != null);

                internalID = counterMessages;
                chatAdapter.notifyItemInserted(internalID);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                buffreader.close();
            }
        } catch (Exception e) {
            // print stack trace.
        } finally {
            // close the file.
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO check that sen_processed characteristic is 1 before sending

                internalID ++;
                ChatModel chatModel = new ChatModel();
                chatModel.setId(String.valueOf(internalID));
                chatModel.setMe("t");


                String message = messagebox.getText().toString().trim();
                chatModel.setMessage(message);
                Integer id_int;
                try{
                    id_int = Integer.parseInt(recipientID);
                    chatModel.setPartnerID(String.valueOf(id_int));
                }
                catch (NumberFormatException ex){
                    Log.e(TAG, "Failed to convert recipient ID from string to int");
                }

                //time
                mCalendar = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CANADA);
                String formattedDate = df.format(mCalendar.getTime());
                chatModel.setTime(formattedDate);

                mBluetoothLeService.writeCharacteristic(sen_text,message);
                mBluetoothLeService.writeCharacteristic(sen_rec_ID,String.valueOf(recipientID));
                mBluetoothLeService.writeCharacteristic(sen_mes_ID, String.valueOf(internalID));
                mBluetoothLeService.writeCharacteristic(sen_processed, "0");

                chatModels.add(chatModel);

                messagebox.setText("");

//                int curSize = chatAdapter.getItemCount();
                chatAdapter.notifyItemInserted(internalID);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }

    //When you click on a message, a toast message shows up
    @Override
    public void onClick(View view, int position) {
        final ChatModel chatInstance = chatModels.get(position);
        final String time = chatInstance.getTime();
        final String messageID = chatInstance.getId();
        final String toToast = String.format("time: %s \nmessageID: %s",time,messageID);
        Toast.makeText(getApplicationContext(),toToast,Toast.LENGTH_SHORT).show();
    }

    //When you long click on a message, go to map activity
    @Override
    public void onLongClick(View view, int position) {

        final ChatModel chatInstance = chatModels.get(position);
        final String time = chatInstance.getTime();
        final String messageID = chatInstance.getId();
        final String toToast = String.format("test");
        final Intent intent = new Intent(ChatActivity.this, MapActivity.class);
        //TODO adjust here the code to actually put lat and longitude
        intent.putExtra("latitude", chatInstance.getPartnerID());
        intent.putExtra("longitude", chatInstance.getPartnerID());
        startActivity(intent);

    }


    private void initCharacteristics(List<BluetoothGattService> gattServices) {
        String uuid;
        String uuidSEND = LoRaUuids.SERVICE_SEND_UUID.toString();
        String uuidREC= LoRaUuids.SERVICE_RECEIVE_UUID.toString();

        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            if (uuid.equals(uuidSEND)){
                sen_rec_ID = gattService.getCharacteristic(LoRaUuids.SEN_CHARACTERISTIC_UUID_REC_ID);
                sen_mes_ID = gattService.getCharacteristic(LoRaUuids.SEN_CHARACTERISTIC_UUID_MES_ID);
                sen_text = gattService.getCharacteristic(LoRaUuids.SEN_CHARACTERISTIC_UUID_TEXT);
                sen_processed = gattService.getCharacteristic(LoRaUuids.SEN_CHARACTERISTIC_UUID_PROCESSED);
            }
            else if(uuid.equals(uuidREC)){
                rec_sen_ID = gattService.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_SEN_ID);
//                rec_rec_ID = gattService.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_REC_ID);
                rec_mes_ID = gattService.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_MES_ID);
                rec_time = gattService.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_TIME);
//                rec_lat = gattService.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_LAT);
//                rec_long = gattService.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_LONG);
                rec_text = gattService.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_TEXT);
                rec_processed = gattService.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_PROCESSED);
            }
        }
        //setup notifications on MessageID Characteristic
        mBluetoothLeService.setCharacteristicNotification(rec_mes_ID,true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }


    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);

        //Write chatModels to file
        try {
            fos = context.openFileOutput(contactName, Context.MODE_PRIVATE); // open the file for writing
            if (fos != null) { // if file the available for writing
                // prepare the file for reading
                OutputStreamWriter chapterWriter = new OutputStreamWriter(fos);
                BufferedWriter buffwriter = new BufferedWriter(chapterWriter);

                ChatModel tempChatModel;
                buffwriter.write(recipientID,0,1);
                for(int i = 0; i < chatModels.size(); i++){
                    tempChatModel = chatModels.get(i);
                    buffwriter.newLine();
                    buffwriter.write(tempChatModel.getId());
                    buffwriter.newLine();
                    buffwriter.write(tempChatModel.getTime());
                    buffwriter.newLine();
                    buffwriter.write(tempChatModel.getMessage());
                    buffwriter.newLine();
                    buffwriter.write(tempChatModel.isMe());
                }
                buffwriter.close();
            }
        } catch (Exception e) {
            // print stack trace.
        } finally {
            // close the file.
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mBluetoothLeService = null;
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_READ_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_RECEIVED);
        return intentFilter;
    }
}