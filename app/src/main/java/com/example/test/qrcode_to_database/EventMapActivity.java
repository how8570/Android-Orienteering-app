package com.example.test.qrcode_to_database;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class EventMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = EventMapActivity.class.getSimpleName();

    private static final String USER_NAME = "how8570";
    /**
     * request code
     */
    private final static int QRCODE_SCAN = 987;
    private String qrcode_msg = "";
    private ArrayList<String> uuids = new ArrayList<>();

    private Toolbar toolbar;
    private GoogleMap mMap;

    private String eventUUID;
    private String title;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<MarkerOptions> markerOptions = new ArrayList<>();

    public static String getJSONString(String rawJson, String key) {
        JSONObject json;
        try {
            json = new JSONObject(rawJson);
            return json.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

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
        toolbar.findViewById(R.id.QRCode_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventMapActivity.this, QRcodeScannerActivity.class);
                startActivityForResult(intent, QRCODE_SCAN);
            }
        });


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

        markers = new ArrayList<>();

        for (MarkerOptions markerOption : getMarkerOptions()) {
            Marker marker = mMap.addMarker(markerOption);
            markers.add(marker);
        }

        // move Camera to first marker with zoom scale 14.0
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), 14));
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
                String uuid = json.getString("pointUUID");
                uuids.add(uuid);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QRCODE_SCAN && resultCode == RESULT_OK) {
            qrcode_msg = Objects.requireNonNull(data).getStringExtra("MSG");
            String locationUUID = getJSONString(qrcode_msg, "locationUUID");
            markers.get(uuids.indexOf(locationUUID)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

            PUNCH_RESULT result = sendPunch();
            switch (result) {
                case OK:
                    markers.get(uuids.indexOf(locationUUID)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    markers.get(uuids.indexOf(locationUUID)).setAlpha(0.6f);
                    Toast.makeText(getApplicationContext(), "成功打卡~", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_ALREADY_PUNCHED:
                    Toast.makeText(getApplicationContext(), "這裡你打過卡了喔", Toast.LENGTH_SHORT).show();
                    markers.get(uuids.indexOf(locationUUID)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    markers.get(uuids.indexOf(locationUUID)).setAlpha(0.6f);
                    break;
                case ERROR_FAIL_DECODE_QRCODE:
                    Toast.makeText(getApplicationContext(), "這不是一個正確的 QR Code", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_SERVER_ERROR:
                    Toast.makeText(getApplicationContext(), "預期外的伺服器錯誤，請稍後再試", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_SERVER_CONNECTION_ERROR:
                    Toast.makeText(getApplicationContext(), "伺服器連線失敗，請檢查網路是否開啟，稍後再嘗試", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_UNKNOWN:
                    Toast.makeText(getApplicationContext(), "發生了預期外的錯誤\nQAQ", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }


        }
    }

    private PUNCH_RESULT sendPunch() {
        final PUNCH_RESULT[] r = {PUNCH_RESULT.ERROR_UNKNOWN};
        Thread thread = new Thread() {
            public void run() {
                StringBuilder response = new StringBuilder();
                try {
                    InetAddress addr = InetAddress.getByName("192.168.1.107"); // get server address
                    URL url = new URL("http://" + addr.getHostAddress() + "/punch"); // parse ipv4 to url

                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoInput(true); // init writer and getInputStream().write() method
                    httpURLConnection.setDoOutput(true); // init reader and getInputStream().read() method
                    httpURLConnection.setRequestProperty("Content-Type", "application/json"); // content-type: json header
                    httpURLConnection.setConnectTimeout(1000);
                    httpURLConnection.setReadTimeout(5000);

                    JSONObject json = null;
                    try {
                        json = new JSONObject(qrcode_msg);
                        json.put("userID", USER_NAME);
                    } catch (JSONException e) {
                        if (json == null) {
                            r[0] = PUNCH_RESULT.ERROR_FAIL_DECODE_QRCODE;
                            return;
                        }
                        e.printStackTrace();
                    }


                    Log.d(TAG, json.toString());
                    DataOutputStream outputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                    outputStream.write(json.toString().getBytes());
                    outputStream.flush();
                    Log.d(TAG, "sent msg:\n" + json.toString());
                    outputStream.close();


                    int code = httpURLConnection.getResponseCode();
                    if (code != 200) {
                        r[0] = PUNCH_RESULT.ERROR_FAIL_DECODE_QRCODE;
                        return;
                    }

                    InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                        response.append("\r");
                    }
                    bufferedReader.close();
                    Log.d(TAG, "receive response:\n" + response.toString());

                    String result = getJSONString(response.toString(), "Result").toUpperCase();
                    switch (result) {
                        case "OK":
                            r[0] = PUNCH_RESULT.OK;
                            break;
                        case "ERROR_ALREADY_PUNCHED":
                            r[0] = PUNCH_RESULT.ERROR_ALREADY_PUNCHED;
                            break;
                        case "ERROR_DB_WRITE_FAIL":
                        case "ERROR_FAIL_DECODE":
                            r[0] = PUNCH_RESULT.ERROR_SERVER_ERROR;
                            break;
                        default:
                            break;
                    }
                } catch (SocketTimeoutException e) {
                    Toast.makeText(getApplicationContext(), "伺服器連線逾時，請稍後再試", Toast.LENGTH_SHORT).show();
                    r[0] = PUNCH_RESULT.ERROR_SERVER_CONNECTION_ERROR;
                    e.printStackTrace();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return r[0];
    }

    private enum PUNCH_RESULT {
        OK,
        ERROR_ALREADY_PUNCHED,
        ERROR_FAIL_DECODE_QRCODE,
        ERROR_SERVER_ERROR,
        ERROR_SERVER_CONNECTION_ERROR,
        ERROR_UNKNOWN
    }


}
