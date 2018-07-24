package com.sanedapps.mapwithlocation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class LocationProvider implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    private final static int REQUEST_CHECK_SETTINGS = 2000;

    private final static int REQUEST_LOCATION_PERMISSION = 800;

    public abstract interface LocationCallbackResult {

        public void handleNewLocation(Location location);
    }

    public static final String TAG = LocationProvider.class.getSimpleName();

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LocationCallbackResult mLocationCallbackListner;

    private Context mContext;

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    private LocationCallback mLocationCallback;

    FusedLocationProviderClient mFusedLocationClient;

    public LocationProvider(Context context, LocationCallbackResult callback) {
        mContext = context;

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationCallbackListner = callback;

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(mContext);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener((Activity) mContext, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener((Activity) mContext, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult((Activity) mContext,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

        // requesting location update callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    mLocationCallbackListner.handleNewLocation(location);
                }
            }
        };
    }

    public void connect() {
        Log.e(TAG, "Connect");
        getLocationWithPermission((AppCompatActivity) mContext);
    }

    public void getLocationWithPermission(AppCompatActivity context) {
        if (PermissionUtils.canMakeSmores(Build.VERSION_CODES.LOLLIPOP_MR1)) {
            if (!PermissionUtils.hasPermissions(mContext, PermissionUtils.GPS_PERMISSION)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.requestPermissions(PermissionUtils.GPS_PERMISSION,
                            REQUEST_LOCATION_PERMISSION);
                }
            } else {
                Log.e(TAG, "Permission is granted");
                mGoogleApiClient.connect();
            }
        } else {
            Log.e(TAG, "Permission is granted Tany");
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        if (mGoogleApiClient.isConnected()) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mGoogleApiClient.disconnect();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "Location services connected.");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener((Activity) mContext, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLocationCallbackListner.handleNewLocation(location);
                        } else {
                            startLocationUpdates();
                        }
                    }
                });


    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution() && mContext instanceof Activity) {
            try {
                Activity activity = (Activity) mContext;
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocationCallbackListner.handleNewLocation(location);
    }

    public Address getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(mContext, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude,
                    1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            return addresses.get(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Handles the activity results
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Toast.makeText(mContext, "User Accept enable location setting", Toast.LENGTH_SHORT).show();
                        connect();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(mContext, "User Refused to enable location setting", Toast.LENGTH_SHORT)
                                .show();

                        break;
                    default:
                        break;
                }
                break;
        }
    }

    /**
     * Handles Permission result
     */
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {
            case 800: {
                if (grantResults.length > 0) {
                    Toast.makeText(mContext, "user accept the permission of the app", Toast.LENGTH_SHORT).show();
                    connect();
                }
            }
        }
    }

    /**
     * Getting adress description from
     */

    public String getAddressDescription(double lat, double lng) {
        Address locationAddress;
        String currentLocation = "";

        locationAddress = getAddress(lat, lng);

        if (locationAddress != null) {

            String address = locationAddress.getAddressLine(0);
            String address1 = locationAddress.getAddressLine(1);
            String city = locationAddress.getLocality();
            String state = locationAddress.getAdminArea();
            String country = locationAddress.getCountryName();
            String postalCode = locationAddress.getPostalCode();

            if (!TextUtils.isEmpty(address)) {
                currentLocation = address;

                if (!TextUtils.isEmpty(address1)) {
                    currentLocation += "\n" + address1;
                }

                if (!TextUtils.isEmpty(city)) {
                    currentLocation += "\n" + city;

                    if (!TextUtils.isEmpty(postalCode)) {
                        currentLocation += " - " + postalCode;
                    }
                } else {
                    if (!TextUtils.isEmpty(postalCode)) {
                        currentLocation += "\n" + postalCode;
                    }
                }

                if (!TextUtils.isEmpty(state)) {
                    currentLocation += "\n" + state;
                }

                if (!TextUtils.isEmpty(country)) {
                    currentLocation += "\n" + country;
                }
            }
        }
        return currentLocation;
    }

}
