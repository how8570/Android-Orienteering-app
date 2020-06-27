package com.example.test.qrcode_to_database;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mTvResult;
    private Button mBtnScanQRCode;
    private Button mBtnSendQuery;

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
        
    }

    private void sendQuery() {
        Thread thread = new Thread() {
            public void run(){
                try {
                    InetAddress addr = InetAddress.getByName("192.168.1.107"); // get server address
                    URL url = new URL("http://"+addr.getHostAddress()); // parse ipv4 to url

                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoInput(true); // init writer and getInputStream().write() method
                    httpURLConnection.setDoOutput(true); // init reader and getInputStream().read() method
                    httpURLConnection.setRequestProperty("Content-Type", "application/json"); // content-type: json header

                    DataOutputStream outputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                    outputStream.write(qrcode_msg.getBytes());
                    outputStream.flush();
                    Log.d(TAG, "sent msg:" + qrcode_msg);
                    outputStream.close();

                    InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String msg = "";
                    while ( (msg = bufferedReader.readLine()) != null) {
                        Log.d(TAG, msg);
                    }

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
                qrcode_msg = data.getStringExtra("MSG");
                mTvResult.setText(qrcode_msg);
            }
        }

    }



}
