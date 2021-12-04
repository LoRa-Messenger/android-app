package com.example.loramessenger;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
//import androidx.lifecycle.ViewModelProvider;

import com.example.loramessenger.databinding.ActivityMainBinding;

import org.jetbrains.annotations.Nullable;

//import io.getstream.chat.android.client.ChatClient;
//import io.getstream.chat.android.client.api.models.FilterObject;
//import io.getstream.chat.android.client.logger.ChatLogLevel;
//import io.getstream.chat.android.client.models.Filters;
//import io.getstream.chat.android.client.models.User;
//import io.getstream.chat.android.livedata.ChatDomain;
//import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel;
//import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModelBinding;
//import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory;
//
//import static java.util.Collections.singletonList;

public final class MainActivity extends AppCompatActivity {

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Step 0 - inflate binding
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        Button chat1 = (Button)findViewById(R.id.board1);
//        Button chat2 = (Button)findViewById(R.id.board2);
//        Button chat3 = (Button)findViewById(R.id.board3);


//        chat1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, SecondActivity.class));
//            }
//        });
//
//        chat2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, ThirdActivity.class));
//            }
//        });
//
//        chat3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, FourthActivity.class));
//            }
//        });


//        // Step 1 - Set up the client for API calls and the domain for offline storage
//        ChatClient client = new ChatClient.Builder("b67pax5b2wdq", getApplicationContext())
//                .logLevel(ChatLogLevel.ALL) // Set to NOTHING in prod
//                .build();
//        new ChatDomain.Builder(client, getApplicationContext()).build();
//
//        // Step 2 - Authenticate and connect the user
//        User user = new User();
//        user.setId("tutorial-droid");
//        user.getExtraData().put("name", "Tutorial Droid");
//        user.getExtraData().put("image", "https://bit.ly/2TIt8NR");
//
//        client.connectUser(
//                user,
//                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidHV0b3JpYWwtZHJvaWQifQ.NhEr0hP9W9nwqV7ZkdShxvi02C5PR7SJE7Cs4y7kyqg"
//        ).enqueue();
//
//        // Step 3 - Set the channel list filter and order
//        // This can be read as requiring only channels whose "type" is "messaging" AND
//        // whose "members" include our "user.id"
//        FilterObject filter = Filters.and(
//                Filters.eq("type", "messaging"),
//                Filters.in("members", singletonList(user.getId()))
//        );
//
//        ChannelListViewModelFactory factory = new ChannelListViewModelFactory(
//                filter,
//                ChannelListViewModel.DEFAULT_SORT
//        );
//
//        ChannelListViewModel channelsViewModel =
//                new ViewModelProvider(this, factory).get(ChannelListViewModel.class);
//
//        // Step 4 - Connect the ChannelListViewModel to the ChannelListView, loose
//        //          coupling makes it easy to customize
//        ChannelListViewModelBinding.bind(channelsViewModel, binding.channelListView, this);
//        binding.channelListView.setChannelItemClickListener(channel -> {
//            // TODO - start channel activity
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //menu.findItem(R.id.menu_refresh).setActionView(null);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_add) {
            Intent mIntent = new Intent(this, AddDeviceActivity.class);
            startActivity(mIntent);
        }

        return super.onOptionsItemSelected(item);
    }


}
