package com.example.loramessenger;

public interface BLEControllerListener {
    public void BLEControllerConnected();
    public void BLEControllerDisconnected();
    public void BLEDeviceFound(String name, String address);
}