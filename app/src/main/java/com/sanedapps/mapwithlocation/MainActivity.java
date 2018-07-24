package com.sanedapps.mapwithlocation;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements LocationProvider.LocationCallbackResult,
        OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private LocationProvider mLocationProvider;

    @BindView(R.id.map)
    MapView mapView;

    @BindView(R.id.btn_location_descritpion)
    Button btn_location_descritpion;

    GoogleMap googleMap;

    Marker myMarker;

    public String mLang, mLat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_detect_location);
        ButterKnife.bind(this);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mLocationProvider = new LocationProvider(this, this);
    }

    public void handleNewLocation(Location location) {
        Log.e("New Location ya brns", location.toString());
        putMapMarker(location.getLatitude(), location.getLongitude());
    }


    @Override
    public void onMapClick(final LatLng latLng) {
        mLang = Double.toString(latLng.latitude);
        mLat = Double.toString(latLng.longitude);
        if (myMarker != null) {
            myMarker.remove();
            putMapMarker(latLng.latitude, latLng.longitude);
        } else {
            putMapMarker(latLng.latitude, latLng.longitude);
        }
    }

    public void putMapMarker(Double lat, Double log) {
        // get location address from the location Provider
        btn_location_descritpion.setText(mLocationProvider.getAddressDescription(lat, log));

        // put marker on map
        LatLng latLng = new LatLng(lat, log);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        MarkerOptions marker = new MarkerOptions().position(
                new LatLng(lat, log));
        marker.icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.location));
        marker.title(getString(R.string.my_location));
        myMarker = googleMap.addMarker(marker);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMapClickListener(this);
        getCurrentLocation();
    }

    void getCurrentLocation() {
        Log.e("TAG", "Start getting the current location");
        mLocationProvider.connect();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        mLocationProvider.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        mLocationProvider.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);
    }
}
