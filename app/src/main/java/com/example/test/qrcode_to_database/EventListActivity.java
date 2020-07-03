package com.example.test.qrcode_to_database;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.qrcode_to_database.tools.Event;
import com.example.test.qrcode_to_database.tools.EventAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;

public class EventListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView mRecyclerList;
    private EventAdapter eventAdapter;

    private ArrayList<Event> events = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        toolbar = findViewById(R.id.toolbar);
        mRecyclerList = findViewById(R.id.mRecyclerList);

        toolbar.setSubtitle("活動列表");
        toolbar.setSubtitleTextColor(0xFFFFFFFF);
        toolbar.inflateMenu(R.menu.menu_event_list);
        toolbar.findViewById(R.id.Refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                events = new ArrayList<>();
                eventAdapter = new EventAdapter(v.getContext(), getList());
                mRecyclerList.setAdapter(eventAdapter);
                Toast.makeText(getApplicationContext(), "嘗試更新，完成", Toast.LENGTH_SHORT).show();
            }
        });

        mRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        eventAdapter = new EventAdapter(this, getList());
        mRecyclerList.setAdapter(eventAdapter);
    }

    private ArrayList<Event> getList() {

        JSONArray jsonArray = getResponse();

        if (jsonArray == null) {
            Toast.makeText(getApplicationContext(), "伺服器連線錯誤，請確認網路連線。"
                    , Toast.LENGTH_LONG).show();
            return events; // TODO make cache result
        }

        for (int i = 0, size = jsonArray.length(); i < size; i++) {
            try {
                JSONObject json = jsonArray.getJSONObject(i);
                Event e = new Event();
                e.setTitle(json.getString("title"));
                e.setDescription(json.getString("content"));
                e.setEventUUID(json.getString("eventUUID"));
                e.setImg(R.mipmap.ic_launcher);
                events.add(e);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return events;
    }

    private JSONArray getResponse() {
        final StringBuilder response = new StringBuilder();
        Thread thread = new Thread() {
            public void run() {
                try {
                    InetAddress addr = InetAddress.getByName("192.168.1.107");
                    URL url = new URL("http://" + addr.getHostAddress() + "/event/names");

                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setDoOutput(true); // init reader and getInputStream().read() method
                    httpURLConnection.setRequestProperty("Content-Type", "application/json"); // content-type: json header
                    httpURLConnection.setConnectTimeout(1000);
                    httpURLConnection.setReadTimeout(5000);

                    InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                        response.append("\r");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        try {
            thread.join(); // waiting to thread finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JSONArray json = null;
        try {
            json = new JSONArray(response.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }


}
