package com.example.test.qrcode_to_database;

import android.os.Bundle;
import android.view.Menu;

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

public class EventMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private Toolbar toolbar;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_map);

        toolbar = findViewById(R.id.toolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        toolbar.setSubtitle("活動名稱");
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

        // Add a marker in taipei and move the camera
        final LatLng TAIPEI_STATION = new LatLng(25.046273, 121.517498);
        final LatLng ZHONGSHAN_STATION = new LatLng(25.052811, 121.520434);

        Marker taipeiStation = mMap.addMarker(new MarkerOptions()
                .position(TAIPEI_STATION)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .alpha(0.7f)
                .title("台北車站"));

        Marker zhongshanStation = mMap.addMarker(new MarkerOptions()
                .position(ZHONGSHAN_STATION)
                .title("中山車站")
                .snippet("尚未跑過"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(TAIPEI_STATION, 14));

        // method to change color
        zhongshanStation.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
    }

}
