package com.example.loramessenger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
//import androidx.lifecycle.ViewModelProvider;

import com.example.loramessenger.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.Nullable;
import android.widget.ListView;

import java.util.ArrayList;

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

    ArrayList<String> listitems = new ArrayList<>();
    ArrayAdapter<String> adapter;
    EditText input;
    Button save;
    ListView listView;
    private BLEController bleController;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        input = findViewById(R.id.EnterDeviceName);
        save = findViewById(R.id.button);
        listView = findViewById(R.id.listView);

        adapter =  new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listitems);
        listView.setAdapter(adapter);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listitems.add(input.getText().toString());
                adapter.notifyDataSetChanged();
                input.setText("");
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    startActivity(new Intent(MainActivity.this, SecondActivity.class));
                }

                if(position == 1){
                    startActivity(new Intent(MainActivity.this, ThirdActivity.class));
                }

                if(position == 2){
                    startActivity(new Intent(MainActivity.this, FourthActivity.class));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_dis) {
            bleController.disconnect();
            startActivity(new Intent(MainActivity.this, ConnectToDeviceActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }


}
