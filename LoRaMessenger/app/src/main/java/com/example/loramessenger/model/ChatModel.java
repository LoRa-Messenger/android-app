package com.example.loramessenger.model;

public class ChatModel {
    private String id;
    private String message;
    private String time;
    private String isMe;
    private String partnerID;
    private String latitude;
    private String longitude;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPartnerID() {
        return partnerID;
    }

    public void setPartnerID(String partnerID) {
        this.partnerID = partnerID;
    }

    public String isMe() {
        return isMe;
    }

    public void setMe(String me) {
        isMe = me;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}