package android.rmit.androidass2;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    protected FusedLocationProviderClient client;
    int MY_PERMISSIONS_REQUEST_LOCATION;
    ArrayList<MarkerData> markerData = new ArrayList<>();


    Address address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestPermission();
        client = LocationServices.getFusedLocationProviderClient(MapsActivity.this);





    }

    private Address geoLocate(MarkerData markerData) {
        Geocoder geocoder = new Geocoder (this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(markerData.getAddress(),1 );
            if(addressList.size() > 0){
                address = addressList.get(0);
                System.out.println("TEsting "+ address.getLongitude()+" ------ " + address.getLatitude());
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
                MarkerData data2 = new MarkerData("Trường Sơn, Phường 2, Tân Bình, Hồ Chí Minh, Việt Nam");
                markerData.add(data);
                markerData.add(data2);
                mMap.addMarker(new MarkerOptions().position(myLocation).title("HCM CIty"));
                createMarker(geoLocate(data).getLatitude(), geoLocate(data).getLongitude(), "heelo");
                createMarker(geoLocate(data2).getLatitude(), geoLocate(data2).getLongitude(), "goodbyeee");

            }
        });
    }
}
