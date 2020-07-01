package com.example.test.qrcode_to_database;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String USER_NAME = "emt";

    private TextView mTvResult;
    private Button mBtnScanQRCode;
    private Button mBtnSendQuery;
    private Button mBtnMap;

    private String qrcode_msg = "";


    /** request code */
    private final static int QRCODE_SCAN = 987;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvResult = findViewById(R.id.mTvResult);
        mBtnScanQRCode = findViewById(R.id.mBtnScanQRCode);
        mBtnSendQuery = findViewById(R.id.mBtnSendQuery);
        mBtnMap = findViewById(R.id.mBtnMap);
        
        mBtnScanQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, QRcodeScannerActivity.class);
                startActivityForResult(intent, QRCODE_SCAN);
            }
        });
        mBtnSendQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendQuery();
            }
        });
        mBtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EventMapActivity.class);
                startActivity(intent);
            }
        });
    }

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

    private void sendQuery() {
        Thread thread = new Thread() {
            public void run(){
                StringBuilder response = new StringBuilder();
                try {
                    Looper.prepare();
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
                            Toast.makeText(getApplicationContext(), "這並不是一個正確的 QR Code..或沒掃", Toast.LENGTH_SHORT).show();
                            Looper.loop();
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
                        Toast.makeText(getApplicationContext(), "這並不是一個正確的 QR Code..", Toast.LENGTH_SHORT).show();
                        Looper.loop();
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
                            Toast.makeText(getApplicationContext(), "成功打卡了", Toast.LENGTH_SHORT).show();
                            break;
                        case "ERROR_ALREADY_PUNCHED":
                            Toast.makeText(getApplicationContext(), "這裡你已經打過了喔 !", Toast.LENGTH_SHORT).show();
                            break;
                        case "ERROR_DB_WRITE_FAIL":
                        case "ERROR_FAIL_DECODE":
                            Toast.makeText(getApplicationContext(), "有一些不知名的錯誤發生，請稍後再試", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), "?????", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    Looper.loop(); // end in thread Toast
                } catch (SocketTimeoutException e) {
                    Toast.makeText(getApplicationContext(), "伺服器連線逾時，請稍後再試", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    Looper.loop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QRCODE_SCAN && resultCode == RESULT_OK) {
            if (mTvResult != null) {
                qrcode_msg = Objects.requireNonNull(data).getStringExtra("MSG");
                mTvResult.setText(qrcode_msg);
            }
        }

    }



}
