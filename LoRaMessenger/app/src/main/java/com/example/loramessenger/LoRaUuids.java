package com.example.loramessenger;

import java.util.UUID;

import static java.util.UUID.fromString;

public class LoRaUuids {
    public static final UUID

            // Service and Characteristic UUIDs
            UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION = fromString("00002902-0000-1000-8000-00805f9b34fb"), // Descriptors
            UUID_DESCRIPTOR_CHARACTERISTIC_PRESENTATION_FORMAT = fromString("00002904-0000-1000-8000-00805f9b34fb"),

            UUID_LED_DISPLAY_SERVICE = fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"),
            UUID_LED_DISPLAY_CHARACTERISTIC = fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"),
            UUID_RANDOM_NUMBER_GENERATOR_SERVICE = fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E"),
            UUID_RANDOM_NUMBER_CHARACTERISTIC = fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E"),

            SERVICE_SEND_UUID = fromString("0f155954-2316-45d0-8791-c18247c6c146"),
            SERVICE_RECEIVE_UUID = fromString("0f0e363b-2cdb-480b-ab3c-e5e784cf68c8"),

            SEN_CHARACTERISTIC_UUID_SEN_ID = fromString("6472961c-3e23-428e-b1f7-efd1e44974b7"),
            SEN_CHARACTERISTIC_UUID_REC_ID = fromString("4a874290-53ea-4f5e-a07b-26a6e9916306"),
            SEN_CHARACTERISTIC_UUID_MES_ID =fromString("fd369597-96ea-4f73-8ac7-4e4111122fd4"),
            SEN_CHARACTERISTIC_UUID_TIME = fromString("df5d4304-42d6-4eea-b6ab-2342af7cffe3"),
            SEN_CHARACTERISTIC_UUID_LAT = fromString("cabedd16-a731-4048-8465-8d1df991fc4c"),
            SEN_CHARACTERISTIC_UUID_LONG = fromString("698f4335-5a1c-48a8-a5b0-3f5ed151408b"),
            SEN_CHARACTERISTIC_UUID_TEXT = fromString("cab0c824-dc00-4257-8876-f3d8e8cc26dd"),
            SEN_CHARACTERISTIC_UUID_PROCESSED = fromString("c78b01d2-15ce-43bd-b3f6-efcdf2f8fc37"),

            REC_CHARACTERISTIC_UUID_SEN_ID = fromString("adeba5b5-d69d-4a26-b91e-1dea9aebfd42"),
            REC_CHARACTERISTIC_UUID_REC_ID = fromString("0a8f5e85-fa36-405b-97fa-5a8b485c2961"),
            REC_CHARACTERISTIC_UUID_MES_ID = fromString("2658e4b8-6c0f-443f-9289-b5dc1fde1ab2"),
            REC_CHARACTERISTIC_UUID_TIME = fromString("ee91aade-5bb7-404d-aa58-db8573086597"),
            REC_CHARACTERISTIC_UUID_LAT = fromString("c059e6c0-4801-4db3-a34f-54c90c026490"),
            REC_CHARACTERISTIC_UUID_LONG = fromString("b61da9aa-3279-4459-ac08-d22dc05c37c0"),
            REC_CHARACTERISTIC_UUID_TEXT = fromString("14868cc4-40dd-4369-aae3-962ef8887b26"),
            REC_CHARACTERISTIC_UUID_PROCESSED = fromString("7d375b33-ab1d-46bd-a71f-212de697cf12");
}
