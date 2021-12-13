package com.example.loramessenger.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loramessenger.R;
import com.example.loramessenger.model.ChatModel;
import com.example.loramessenger.ItemClickListener;

import java.util.ArrayList;

public class ChatAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ChatModel> chatModels;
    private Context context;
    private ItemClickListener clickListener;

    public ChatAdapter(ArrayList<ChatModel>chatModels, Context context){
        this.chatModels = chatModels;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return chatModels.size();
    }

    //Returns the view type of the item at position for the purposes of view recycling.
    @Override
    public int getItemViewType(int position) {
        if (chatModels.get(position).isMe().equals("t")) {
            return 1;
        } else {
            return 2;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        RecyclerView.ViewHolder viewHolder;
        if(viewType == 1){
            View v1 = layoutInflater.inflate(R.layout.my_message, parent, false);
            viewHolder = new ViewHolder1(v1);
        }
        else{
            View v2 = layoutInflater.inflate(R.layout.received_message, parent, false);
            viewHolder = new ViewHolder2(v2);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder.getItemViewType() == 1){
            ViewHolder1 vh1 = (ViewHolder1) holder;
            vh1.message.setText(chatModels.get(position).getMessage());
        }
        else{
            ViewHolder2 vh2 = (ViewHolder2) holder;
            vh2.message.setText(chatModels.get(position).getMessage());
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public class ViewHolder1 extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView message;

        public ViewHolder1(View itemView){
            super(itemView);
            this.message = itemView.findViewById(R.id.message_body);
            itemView.setOnClickListener(this); // bind the listener
        }
        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

    public class ViewHolder2 extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView message;

        public ViewHolder2(View itemView){
            super(itemView);
            this.message = itemView.findViewById(R.id.message_body);
            itemView.setOnClickListener(this); // bind the listener
        }
        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }
}