package com.example.loramessenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.loramessenger.adapter.ChatAdapter;
import com.example.loramessenger.model.ChatModel;

import java.util.ArrayList;

public class ThirdActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messagebox;
    private Button btnSend, btnReceive;
    final ArrayList<ChatModel> chatModels = new ArrayList<>();
    int internalID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);


        recyclerView = findViewById(R.id.chat_list);
        messagebox = findViewById(R.id.et_chat_box);
        btnSend = findViewById(R.id.btn_chat_send);
        btnReceive = findViewById(R.id.btn_chat_receive);

        ChatAdapter chatAdapter = new ChatAdapter(chatModels, ThirdActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ThirdActivity.this));
        recyclerView.setAdapter(chatAdapter);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                internalID ++;
                ChatModel chatModel = new ChatModel();
                chatModel.setId(internalID);
                chatModel.setMe(true);
                chatModel.setMessage(messagebox.getText().toString().trim());
                chatModels.add(chatModel);

                messagebox.setText("");

//                int curSize = chatAdapter.getItemCount();
                chatAdapter.notifyItemInserted(internalID);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
        //TODO remove and actually put the bluetooth received data here
        btnReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                internalID ++;
                ChatModel chatModel = new ChatModel();
                chatModel.setId(internalID);
                chatModel.setMe(false);
                chatModel.setMessage(messagebox.getText().toString().trim());
                chatModels.add(chatModel);
                messagebox.setText("");
                chatAdapter.notifyItemInserted(internalID);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }
}