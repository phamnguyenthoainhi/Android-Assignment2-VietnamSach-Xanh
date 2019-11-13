package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    protected FusedLocationProviderClient client;
    int MY_PERMISSIONS_REQUEST_LOCATION;
    ArrayList<MarkerData> markerData = new ArrayList<>();
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    Button next;


    FusedLocationProviderClient fusedLocationProviderClient;
    Address address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        next = findViewById(R.id.nxtbtn);
        Button toSignIn = findViewById(R.id.toSignIn);

        toSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, SignInActivity.class));
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, SitesActivity.class));
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestPermission();
        client = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
//        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//                Toast.makeText(MapsActivity.this, location.getLatitude()+ " "+ location.getLongitude(), Toast.LENGTH_SHORT).show();
//                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                MarkerData data = new MarkerData("702 Nguyễn Văn Linh, Tân Hưng, Quận 7, Hồ Chí Minh 700000, Việt Nam");
//                MarkerData data2 = new MarkerData("Trường Sơn, Phường 2, Tân Bình, Hồ Chí Minh, Việt Nam");
//                markerData.add(data);
//                markerData.add(data2);
//
//                mMap.addMarker(new MarkerOptions().position(myLocation).title("HCM CIty"));
//                createMarker(geoLocate(data).getLatitude(), geoLocate(data).getLongitude(), "heelo");
//                createMarker(geoLocate(data2).getLatitude(), geoLocate(data2).getLongitude(), "goodbyeee");
//                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLocation, 10);
//                mMap.animateCamera(cameraUpdate);
//            }
//        });




    }

    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    private Address geoLocate(MarkerData markerData) {
        Geocoder geocoder = new Geocoder (this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(markerData.getAddress(),1 );
            if(addressList.size() > 0){
                address = addressList.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }


    public Marker createMarker(double latitude, double longitude, String title) {

        return mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title));
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MapsActivity.this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
    }

    @SuppressLint("MissingPermission")
    public void getPosition(View view) {
        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                Toast.makeText(MapsActivity.this, location.getLatitude()+ " "+ location.getLongitude(), Toast.LENGTH_SHORT).show();
                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerData data = new MarkerData("702 Nguyễn Văn Linh, Tân Hưng, Quận 7, Hồ Chí Minh 700000, Việt Nam");
                data.setTitle("data1");
                MarkerData data2 = new MarkerData("Trường Sơn, Phường 2, Tân Bình, Hồ Chí Minh, Việt Nam");
                data2.setTitle("data2");
                markerData.add(data);
                markerData.add(data2);
                mMap.addMarker(new MarkerOptions().position(myLocation).title("My "));

                for (int i = 0; i < markerData.size() ; i++) {
                    createMarker(geoLocate(markerData.get(i)).getLatitude(), geoLocate(markerData.get(i)).getLongitude(), markerData.get(i).getTitle());
                }
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLocation, 10);
                mMap.animateCamera(cameraUpdate);
            }
        });
    }
}
