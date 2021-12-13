package com.example.loramessenger.model;

import android.content.Context;

import java.util.ArrayList;

public class ChatContact {
    private String name;
    private String id;

    public ChatContact(String name, String id){
        this.name = name;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
