//package com.example.loramessenger;
//
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothGattService;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//import android.util.Log;
//
//public class BleUtils {
//    private static List<BluetoothGattCharacteristic> findCharacteristics(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, int property) {
//        if (serviceUuid == null) {
//            return null;
//        }
//
//        if (characteristicUuid == null) {
//            return null;
//        }
//
//        if (gatt == null) {
//            return null;
//        }
//
//        BluetoothGattService service = gatt.getService(serviceUuid);
//        if (service == null) {
//            return null;
//        }
//
//        List<BluetoothGattCharacteristic> results = new ArrayList<>();
//        for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
//            int props = c.getProperties();
//            if (characteristicUuid.equals(c.getUuid())
//                    && (property == (property & props))) {
//                results.add(c);
//            }
//        }
//        return results;
//    }
//
//    public static boolean readCharacteristic(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid) {
//        if (gatt == null) {
//            return false;
//        }
//        BluetoothGattService service = gatt.getService(serviceUuid);
//        if (service == null) {
//            return false;
//        }
//        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
//        if (characteristic == null) {
//            return false;
//        }
//        return gatt.readCharacteristic(characteristic);
//    }
//
//    public static boolean readCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//        if (gatt == null || characteristic == null) {
//            return false;
//        }
//        return gatt.readCharacteristic(characteristic);
//    }
//
//    public static boolean setCharacteristicNotification(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid, boolean enable) {
//        if (gatt == null) {
//            return false;
//        }
//        BluetoothGattService service = gatt.getService(serviceUuid);
//        if (service == null) {
//            return false;
//        }
//        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
//
//        if (characteristic == null) {
//            Log.d("[UTIL]","could not get characteristic: " + characteristicUuid.toString()+ " for service: " + serviceUuid.toString());
//            return false;
//        }
//
//        if (!gatt.setCharacteristicNotification(characteristic, true)) {
//            Log.d("[UTIL]","was not able to setCharacteristicNotification");
//            return false;
//        }
//
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUuid);
//        if (descriptor == null) {
//            Log.d("[UTIL]","was not able to getDescriptor");
//            return false;
//        }
//
//        if (enable) {
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        } else {
//            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//        }
//        return gatt.writeDescriptor(descriptor);
//    }
//
//    // TODO consolidate with setCharacteristicNotification - duplicated code because we didn't have
//    // time to run regression
//    public static boolean unsetCharacteristicNotification(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid, boolean enable) {
//        if (gatt == null) {
//            return false;
//        }
//        BluetoothGattService service = gatt.getService(serviceUuid);
//        if (service == null) {
//            return false;
//        }
//        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
//
//        if (characteristic == null) {
//            Log.d("[UTIL]","could not get characteristic: " + characteristicUuid.toString() +" for service: "+ serviceUuid.toString());
//            return false;
//        }
//
//        if (!gatt.setCharacteristicNotification(characteristic, false)) {
//            Log.d("[UTIL]","was not able to setCharacteristicNotification");
//            return false;
//        }
//
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUuid);
//        if (descriptor == null) {
//            Log.d("[UTIL]","was not able to getDescriptor");
//            return false;
//        }
//
//        if (enable) {
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        } else {
//            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//        }
//
//        return gatt.writeDescriptor(descriptor);
//    }
//
//    public static boolean writeCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int value, int format, int offset) {
//        if (gatt == null) {
//            return false;
//        }
//        Log.d("[UTIL]","prop: " + characteristic.getProperties());
//        characteristic.setValue(value, format, offset);
//        return gatt.writeCharacteristic(characteristic);
//    }
//
//    public static boolean writeCharacteristic(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, int value, int format, int offset) {
//        if (gatt == null) {
//            return false;
//        }
//        BluetoothGattCharacteristic characteristic = gatt.getService(serviceUuid).getCharacteristic(characteristicUuid);
//        return writeCharacteristic(gatt, characteristic, value, format, offset);
//    }
//
//
//    // TODO modify calls to writeCharacteristic() to use findCharacteristic to match iOS behavior
//    public static boolean writeCharacteristics(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, int value, int format, int offset) {
//        List<BluetoothGattCharacteristic> characteristics = BleUtils.findCharacteristics(gatt, serviceUuid, characteristicUuid, BluetoothGattCharacteristic.PROPERTY_WRITE);
//        if (characteristics == null || characteristics.size() == 0) {
//            return false;
//        }
//
//        boolean submitted = false;
//        boolean result = false;
//        for (BluetoothGattCharacteristic characteristic :
//                characteristics) {
//            if (characteristic.setValue(value, format, offset)) {
//                submitted = true;
//                result = gatt.writeCharacteristic(characteristic);
//            }
//        }
//        Log.d("[UTIL]","submitted: " + submitted);
//        return result;
//    }
//
//    public static boolean writeCharacteristic(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, byte[] value) {
//        if (gatt == null) {
//            return false;
//        }
//        BluetoothGattCharacteristic characteristic = gatt.getService(serviceUuid).getCharacteristic(characteristicUuid);
//        boolean submitted = characteristic.setValue(value);
//        Log.d("[UTIL]","submitted: " + submitted);
//        return gatt.writeCharacteristic(characteristic);
//    }
//
//
//    public static boolean setCharacteristicIndication(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid, boolean enable) {
//        if (gatt == null) {
//            return false;
//        }
//        BluetoothGattService service = gatt.getService(serviceUuid);
//        if (service == null) {
//            return false;
//        }
//        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
//
//        if (characteristic == null) {
//            Log.d("[UTIL]", "could not get characteristic: " + characteristicUuid.toString() + " for service: " + serviceUuid.toString());
//            return false;
//        }
//
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUuid);
//        if (descriptor == null) {
//            Log.d("[UTIL]", "was not able to getDescriptor");
//            return false;
//        }
//
//        if (enable) {
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
//        } else {
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
//        }
//
//        return gatt.writeDescriptor(descriptor);
//    }
//
//    public static boolean setCharacteristicIndications(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid, boolean enable) {
//        List<BluetoothGattCharacteristic> characteristics = BleUtils.findCharacteristics(gatt,
//                serviceUuid, characteristicUuid, BluetoothGattCharacteristic.PROPERTY_INDICATE);
//        if (characteristics == null || characteristics.size() == 0) {
//            return false;
//        }
//
//        boolean submitted = false;
//        boolean result = false;
//        for (BluetoothGattCharacteristic characteristic :
//                characteristics) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUuid);
//            if (descriptor == null) {
//                Log.d("[UTIL]", "was not able to getDescriptor");
//                return false;
//            }
//
//            if (enable) {
//                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
//            } else {
//                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//            }
//            result = gatt.writeDescriptor(descriptor);
//        }
//        Log.d("[UTIL]", "submitted: " + submitted);
//        return result;
//    }
//}
