package com.example.loramessenger;

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
            BluetoothGattService RandomNumService = bluetoothGatt.getService(LoRaUuids.UUID_RANDOM_NUMBER_GENERATOR_SERVICE);
            BluetoothGattService screenService = bluetoothGatt.getService(LoRaUuids.UUID_LED_DISPLAY_SERVICE);
            //mRandomNumber = RandomNumService.getCharacteristic(LoRaUuids.UUID_RANDOM_NUMBER_CHARACTERISTIC);
            //mLEDScreen = screenService.getCharacteristic(LoRaUuids.UUID_LED_DISPLAY_CHARACTERISTIC);


            for (BluetoothGattService service : gatt.getServices()) {
                Log.i("BLE", String.valueOf(service));


                if (service.getUuid().equals(LoRaUuids.UUID_LED_DISPLAY_SERVICE)) {
                    Log.i("BLE", "Inside LED display Service");
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        Log.i("BLE", "Inside LED display Characteristic");
                        if (characteristic.getUuid().equals(LoRaUuids.UUID_LED_DISPLAY_CHARACTERISTIC)) {
                            Log.i("BLE", "Char = " + LoRaUuids.UUID_LED_DISPLAY_CHARACTERISTIC);
                            Log.i("GATT", "gatt = " +gatt);
                            Log.i("CHAR","characteristic= "+characteristic);
                            read = false;
//                            bluetoothGatt.readCharacteristic(mLEDScreen);
                            btGattChar = characteristic;
                            chars.add(btGattChar);
                            fireConnected();
                            Log.i("BLE", "LED display Tracking Added");
                        }
                    }
                }
            }
        }

        // Acceleration only
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            UUID uuid = characteristic.getUuid();
            byte[] ba = characteristic.getValue();
            Log.i("BLE","Inside onCharacteristicChanged");
            Log.i("BLE","Value of ba= "+ba);

            if (ba == null || ba.length == 0) {
                Log.i("BLE", "characteristic is not initialized");
            } else {
                if (LoRaUuids.UUID_RANDOM_NUMBER_CHARACTERISTIC.equals(uuid)) {
                    ba = characteristic.getValue();
                    Log.i("BLE","Value of ba= "+ba);
                }
            }
        }
    };

    private void setNotifySensor(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic = gatt.getService(LoRaUuids.UUID_RANDOM_NUMBER_GENERATOR_SERVICE).getCharacteristic(LoRaUuids.UUID_RANDOM_NUMBER_CHARACTERISTIC);
        gatt.setCharacteristicNotification(characteristic, true);

        BluetoothGattDescriptor desc = characteristic.getDescriptor(LoRaUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION);
        Log.d("BLE", "Descriptor is " + desc); // this is not null
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        boolean write_desc = gatt.writeDescriptor(desc);
        Log.d("BLE", "Descriptor write: " +write_desc); // returns true

    }
    public void requestCharacteristics(BluetoothGatt gatt) {
        for(BluetoothGattCharacteristic test: chars){
            boolean temp = gatt.readCharacteristic(test);
            Log.d("TEST", "read characteristic: " + temp);
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
            setNotifySensor(bluetoothGatt);
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