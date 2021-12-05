package com.example.loramessenger;

import static com.example.loramessenger.LoRaUuids.SERVICE_RECEIVE_UUID;
import static com.example.loramessenger.LoRaUuids.SERVICE_SEND_UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BLEController {

    private static BLEController instance;

    private BluetoothLeScanner scanner;
    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;
    private BluetoothManager bluetoothManager;

    BluetoothGattCharacteristic mRandomNumber;
    BluetoothGattCharacteristic mLEDScreen;
    BluetoothGatt mGatt;
    BluetoothGattService mSender;           // Service for Sender
    BluetoothGattService mReceiver;         // Service for Receiver
    BluetoothGattCharacteristic sen_sen_ID;
    BluetoothGattCharacteristic sen_rec_ID;
    BluetoothGattCharacteristic sen_mes_ID;
    BluetoothGattCharacteristic sen_time;
    BluetoothGattCharacteristic sen_lat;
    BluetoothGattCharacteristic sen_long;
    BluetoothGattCharacteristic sen_text;
    BluetoothGattCharacteristic sen_processed;

    BluetoothGattCharacteristic rec_sen_ID;
    BluetoothGattCharacteristic rec_rec_ID;
    BluetoothGattCharacteristic rec_mes_ID;
    BluetoothGattCharacteristic rec_time;
    BluetoothGattCharacteristic rec_lat;
    BluetoothGattCharacteristic rec_long;
    BluetoothGattCharacteristic rec_text;
    BluetoothGattCharacteristic rec_processed;
    public boolean read = true;

    private BluetoothGattCharacteristic btGattChar = null;
    private BluetoothGattDescriptor btGattDesc = null;

    private ArrayList<BLEControllerListener> listeners = new ArrayList<>();
    private HashMap<String, BluetoothDevice> devices = new HashMap<>();
    List<BluetoothGattCharacteristic> chars = new ArrayList<>();

    private BLEController(Context ctx) {
        this.bluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public static BLEController getInstance(Context ctx) {
        if (null == instance)
            instance = new BLEController((ctx));

        return instance;
    }

    public void addBLEControllerListener(BLEControllerListener l) {
        if (!this.listeners.contains(l))
            this.listeners.add(l);
    }

    public void removeBLEControllerListener(BLEControllerListener l) {
        this.listeners.remove(l);
    }

    public void init() {
        this.devices.clear();
        Log.i("[SCAN]", "inside init");
        this.scanner = this.bluetoothManager.getAdapter().getBluetoothLeScanner();
        scanner.startScan(bleCallback);
    }

    private ScanCallback bleCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (!devices.containsKey(device.getAddress()) && isThisTheDevice(device)) {
                deviceFound(device);
                Log.i("[SCAN]", "device found");
            }
            Log.i("[SCAN]", "inside onScanResult");
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                BluetoothDevice device = sr.getDevice();
                if (!devices.containsKey(device.getAddress()) && isThisTheDevice(device)) {
                    deviceFound(device);
                }
                Log.i("[SCAN]", "inside onBatchScanResults");
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i("[BLE]", "scan failed with errorcode: " + errorCode);
        }
    };

    private boolean isThisTheDevice(BluetoothDevice device) {
        Log.i("[BLE]", "scan compare with" + ConnectToDeviceActivity.name);
        return null != device.getName() && device.getName().startsWith(ConnectToDeviceActivity.name);
    }

    private void deviceFound(BluetoothDevice device) {
        this.devices.put(device.getAddress(), device);
        fireDeviceFound(device);
    }

    public void connectToDevice(String address) {
        Log.i("[BLE]","inside connect to device method");
        this.device = this.devices.get(address);
        this.scanner.stopScan(this.bleCallback);
        Log.i("[BLE]", "connect to device " + device.getAddress());
        this.bluetoothGatt = device.connectGatt(null, false, this.bleConnectCallback);
    }

    private BluetoothGattCallback bleConnectCallback = new BluetoothGattCallback() {
        String str;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("[BLE]", "start service discovery " + bluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                btGattChar = null;
                Log.w("[BLE]", "DISCONNECTED with status " + status);
                fireDisconnected();
            } else {
                Log.i("[BLE]", "unknown state " + newState + " and status " + status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d("BLE", "onServiceDiscovered callback executed");
            mSender = bluetoothGatt.getService(SERVICE_SEND_UUID);
            mReceiver = bluetoothGatt.getService(SERVICE_RECEIVE_UUID);
            sen_sen_ID = mSender.getCharacteristic(LoRaUuids.SEN_CHARACTERISTIC_UUID_SEN_ID);
            sen_rec_ID = mSender.getCharacteristic(LoRaUuids.SEN_CHARACTERISTIC_UUID_REC_ID);
            sen_mes_ID = mSender.getCharacteristic(LoRaUuids.SEN_CHARACTERISTIC_UUID_MES_ID);
            sen_time = mSender.getCharacteristic(LoRaUuids.SEN_CHARACTERISTIC_UUID_TIME);
            sen_lat = mSender.getCharacteristic(LoRaUuids.SEN_CHARACTERISTIC_UUID_LAT);
            sen_long = mSender.getCharacteristic(LoRaUuids.SEN_CHARACTERISTIC_UUID_LONG);
            sen_text = mSender.getCharacteristic(LoRaUuids.SEN_CHARACTERISTIC_UUID_TEXT);
            sen_processed = mSender.getCharacteristic(LoRaUuids.SEN_CHARACTERISTIC_UUID_PROCESSED);

            rec_sen_ID = mReceiver.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_SEN_ID);
            rec_rec_ID = mReceiver.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_REC_ID);
            rec_mes_ID = mReceiver.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_MES_ID);
            rec_time = mReceiver.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_TIME);
            rec_lat = mReceiver.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_LAT);
            rec_long = mReceiver.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_LONG);
            rec_text = mReceiver.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_TEXT);
            rec_processed = mReceiver.getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_PROCESSED);

            for (BluetoothGattService service : gatt.getServices()) {
                Log.d("BLE", String.valueOf(service));
                if (service.getUuid().equals(SERVICE_SEND_UUID)) {
                    read = false;
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        if (characteristic.getUuid().equals(LoRaUuids.SEN_CHARACTERISTIC_UUID_SEN_ID)) {
                            //bluetoothGatt.readCharacteristic(sen_sen_ID);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Sender Service", "Sender ID added" );
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.SEN_CHARACTERISTIC_UUID_REC_ID)) {
                            //bluetoothGatt.readCharacteristic(sen_rec_ID);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Sender Service", "Receiver ID added");
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.SEN_CHARACTERISTIC_UUID_MES_ID)) {
                            //bluetoothGatt.readCharacteristic(sen_mes_ID);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Sender Service", "Message ID added");
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.SEN_CHARACTERISTIC_UUID_TIME)) {
                            //bluetoothGatt.readCharacteristic(sen_time);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Sender Service", "Time stamp added");
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.SEN_CHARACTERISTIC_UUID_LAT)) {
                            //bluetoothGatt.readCharacteristic(sen_lat);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Sender Service", "Latitude added");
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.SEN_CHARACTERISTIC_UUID_LONG)) {
                            //bluetoothGatt.readCharacteristic(sen_long);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Sender Service", "Longitude added");
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.SEN_CHARACTERISTIC_UUID_TEXT)) {
                            //bluetoothGatt.readCharacteristic(sen_text);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Sender Service", "Text added");
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.SEN_CHARACTERISTIC_UUID_PROCESSED)) {
                            //bluetoothGatt.readCharacteristic(sen_processed);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Sender Service", "Processed Characteristic added");
                        }
                    }
                }
                if (service.getUuid().equals(SERVICE_RECEIVE_UUID)) {
                    read = true;
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        if (characteristic.getUuid().equals(LoRaUuids.REC_CHARACTERISTIC_UUID_SEN_ID)) {
                            bluetoothGatt.readCharacteristic(rec_sen_ID);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Receiver Service", "Sender ID added. Value = " +characteristic.getValue());
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.REC_CHARACTERISTIC_UUID_REC_ID)) {
                            bluetoothGatt.readCharacteristic(rec_rec_ID);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Receiver Service", "Receiver ID added. Value = " +characteristic.getValue());
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.REC_CHARACTERISTIC_UUID_MES_ID)) {
                            bluetoothGatt.readCharacteristic(rec_mes_ID);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Receiver Service", "Message ID added. Value = " +characteristic.getValue());
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.REC_CHARACTERISTIC_UUID_TIME)) {
                            bluetoothGatt.readCharacteristic(rec_time);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Receiver Service", "Time stamp added. Value = " +characteristic.getValue());
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.REC_CHARACTERISTIC_UUID_LAT)) {
                            bluetoothGatt.readCharacteristic(rec_lat);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Receiver Service", "Latitude added Value = " +characteristic.getValue());
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.REC_CHARACTERISTIC_UUID_LONG)) {
                            bluetoothGatt.readCharacteristic(rec_long);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Receiver Service", "Longitude added Value = " +characteristic.getValue());
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.REC_CHARACTERISTIC_UUID_TEXT)) {
                            bluetoothGatt.readCharacteristic(rec_text);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Receiver Service", "Text added. Value = " +characteristic.getValue());
                        }
                        if (characteristic.getUuid().equals(LoRaUuids.REC_CHARACTERISTIC_UUID_PROCESSED)) {
                            bluetoothGatt.readCharacteristic(rec_processed);
                            chars.add(characteristic);
                            //requestCharacteristics(gatt);
                            fireConnected();
                            Log.d("Receiver Service", "Processed Characteristic added. Value = " +characteristic.getValue());
                        }
                    }
                }
            }
        }

//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            super.onServicesDiscovered(gatt, status);
//            BluetoothGattService RandomNumService = bluetoothGatt.getService(LoRaUuids.UUID_RANDOM_NUMBER_GENERATOR_SERVICE);
//            BluetoothGattService screenService = bluetoothGatt.getService(LoRaUuids.UUID_LED_DISPLAY_SERVICE);
//            //mRandomNumber = RandomNumService.getCharacteristic(LoRaUuids.UUID_RANDOM_NUMBER_CHARACTERISTIC);
//            //mLEDScreen = screenService.getCharacteristic(LoRaUuids.UUID_LED_DISPLAY_CHARACTERISTIC);
//
//
//            for (BluetoothGattService service : gatt.getServices()) {
//                Log.i("BLE", String.valueOf(service));
//
//
//                if (service.getUuid().equals(LoRaUuids.UUID_LED_DISPLAY_SERVICE)) {
//                    Log.i("BLE", "Inside LED display Service");
//                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
//                        Log.i("BLE", "Inside LED display Characteristic");
//                        if (characteristic.getUuid().equals(LoRaUuids.UUID_LED_DISPLAY_CHARACTERISTIC)) {
//                            Log.i("BLE", "Char = " + LoRaUuids.UUID_LED_DISPLAY_CHARACTERISTIC);
//                            Log.i("GATT", "gatt = " +gatt);
//                            Log.i("CHAR","characteristic= "+characteristic);
//                            read = false;
////                            bluetoothGatt.readCharacteristic(mLEDScreen);
//                            btGattChar = characteristic;
//                            chars.add(btGattChar);
//                            fireConnected();
//                            Log.i("BLE", "LED display Tracking Added");
//                        }
//                    }
//                }
//            }
//        }

//        // Acceleration only
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            super.onCharacteristicChanged(gatt, characteristic);
//            UUID uuid = characteristic.getUuid();
//            byte[] ba = characteristic.getValue();
//            Log.i("BLE", "Inside onCharacteristicChanged");
//            Log.i("BLE", "Value of ba= " + ba);
//
//            if (ba == null || ba.length == 0) {
//                Log.i("BLE", "characteristic is not initialized");
//            } else {
//                if (LoRaUuids.SEN_CHARACTERISTIC_UUID_SEN_ID.equals(uuid)) {
//                    ba = characteristic.getValue();
//                    Log.i("BLE", "Value of ba= " + ba);
//                }
//            }
//        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("BLE", "onCharacteristicRead executed");
            UUID uuid = characteristic.getUuid();


            if (status== bluetoothGatt.GATT_SUCCESS) {

                if (LoRaUuids.REC_CHARACTERISTIC_UUID_SEN_ID.equals(uuid)) {
                    //int senSenderID = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                    str = toHexString(characteristic.getValue(), characteristic.getValue().length);
                    Log.d("CharRead", "Sen Sender ID = " + str);
                    chars.add(characteristic);

                }
                if (LoRaUuids.REC_CHARACTERISTIC_UUID_REC_ID.equals(uuid)) {
                    //int senRecID = (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0));
                    str = toHexString(characteristic.getValue(), characteristic.getValue().length);
                    Log.d("CharRead", "Sen Receiver ID = " + str);
                    chars.add(characteristic);

                }
                if (LoRaUuids.REC_CHARACTERISTIC_UUID_MES_ID.equals(uuid)){
                    str = toHexString(characteristic.getValue(), characteristic.getValue().length);
                    Log.d("CharRead", "Sen Message ID " + str);
                    chars.add(characteristic);
                }
                if (LoRaUuids.REC_CHARACTERISTIC_UUID_TEXT.equals(uuid)){
                    str = toHexString(characteristic.getValue(), characteristic.getValue().length);
                    Log.d("CharRead", " Sen Text : " + str);
                    chars.add(characteristic);
                }

//                chars.remove(chars.get(chars.size() - 1));
//
//                if (chars.size() > 0) {
//                    requestCharacteristics(gatt);   // For Reading multiple times, not only once (Dec 1)
//                } else {
//                    gatt.disconnect();
//                }
            }
        }
    };

    private void setNotifySensor(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic = gatt.getService(LoRaUuids.SERVICE_RECEIVE_UUID).getCharacteristic(LoRaUuids.REC_CHARACTERISTIC_UUID_SEN_ID);
        gatt.setCharacteristicNotification(characteristic, true);

//        BluetoothGattDescriptor desc = characteristic.getDescriptor(LoRaUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION);
//        Log.d("BLE", "Descriptor is " + desc); // this is not null
//        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        boolean write_desc = gatt.writeDescriptor(desc);
//        Log.d("BLE", "Descriptor write: " +write_desc); // returns true

    }
    public void requestCharacteristics(BluetoothGatt gatt) {
        for(BluetoothGattCharacteristic test: chars){
            gatt.readCharacteristic(test);
            //boolean temp = gatt.readCharacteristic(test);
            //Log.d("TEST", "read characteristic: " + temp);
        }
    }


    private void fireDisconnected() {
        for (BLEControllerListener l : this.listeners)
            l.BLEControllerDisconnected();

        this.device = null;
    }

    private void fireConnected() {
        for (BLEControllerListener l : this.listeners)
            l.BLEControllerConnected();
    }

    private void fireDeviceFound(BluetoothDevice device) {
        for (BLEControllerListener l : this.listeners)
            l.BLEDeviceFound(device.getName().trim(), device.getAddress());
    }


    public void read_write_Data(byte[] data) {
        if (read ==  true) {
            requestCharacteristics(bluetoothGatt);
            //setNotifySensor(bluetoothGatt);
            //Log.d("Read","Read is true");
        }else{
            for(BluetoothGattCharacteristic test: chars){
                byte[] a = new byte[2];
                a[0] = (byte) 0x68;
                a[1] = (byte) 0x69;
                test.setValue(a);
                boolean temp = bluetoothGatt.writeCharacteristic(test);
                Log.d("TEST", "write characteristic: " + temp);
            }
        }
    }

    public String toHexString(byte[] mBytes, int mLength) {
        char[] dst = new char[mLength * 2];
        char[] hexArray = "0123456789ABCDEF".toCharArray();

        for (int si = 0, di = 0; si < mLength; si++) {
            byte b = mBytes[si];
            dst[di++] = hexArray[(b & 0xf0) >>> 4];
            dst[di++] = hexArray[(b & 0x0f)];
        }

        return new String(dst);
    }


    public void disconnect() {
        this.bluetoothGatt.disconnect();
    }
}