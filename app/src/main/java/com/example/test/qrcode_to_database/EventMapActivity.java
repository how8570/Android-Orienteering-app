package com.example.test.qrcode_to_database;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.util.Objects;

public class EventMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private Toolbar toolbar;
    private GoogleMap mMap;

    private String eventUUID;
    private String title;

    private ArrayList<MarkerOptions> markerOptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_map);

        Bundle bundle = this.getIntent().getExtras();
        eventUUID = Objects.requireNonNull(bundle).getString("eventUUID");
        title = bundle.getString("title");

        toolbar = findViewById(R.id.toolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        toolbar.setSubtitle(title);
        toolbar.setSubtitleTextColor(0xFFFFFFFF);
        toolbar.inflateMenu(R.menu.menu_event_map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        markerOptions = new ArrayList<>();
        markerOptions = getMarkerOptions();

        for (MarkerOptions markerOption : markerOptions) {
            mMap.addMarker(markerOption);
        }

        // move Camera to first marker with zoom scale 14.0
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.get(0).getPosition(), 14));
//        // method to change color
//        markerName.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
    }

    private ArrayList<MarkerOptions> getMarkerOptions() {
        JSONObject jsonObject = getResponse(eventUUID);

        if (jsonObject == null) {
            Toast.makeText(getApplicationContext(), "伺服器連線錯誤，請確認網路連線。"
                    , Toast.LENGTH_LONG).show();
            return markerOptions; // TODO make cache result
        }

        JSONArray pointsArray = null;
        try {
            pointsArray = jsonObject.getJSONArray("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (pointsArray == null) {
            Toast.makeText(getApplicationContext(), "預期外的伺服器回應錯誤，請稍後再試。"
                    , Toast.LENGTH_LONG).show();
            return markerOptions; // TODO make cache result
        }

        for (int i = 0, size = pointsArray.length(); i < size; i++) {
            try {
                JSONObject json = pointsArray.getJSONObject(i);
                LatLng latLng = new LatLng(json.getDouble("latitude")
                        , json.getDouble("longitude"));
                MarkerOptions m = new MarkerOptions();
                m.position(latLng);
                m.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                m.title(json.getString("title"));
                m.snippet(json.getString("content"));
                markerOptions.add(m);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return markerOptions;
    }

    private JSONObject getResponse(final String eventUUID) {
        final StringBuilder response = new StringBuilder();
        Thread thread = new Thread() {
            public void run() {
                try {
                    InetAddress addr = InetAddress.getByName("192.168.1.107");
                    URL url = new URL("http://" + addr.getHostAddress()
                            + "/event/" + eventUUID + "/points");

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
        Log.d("##check response", response.toString());

        JSONObject json = null;
        try {
            json = new JSONObject(response.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

}
