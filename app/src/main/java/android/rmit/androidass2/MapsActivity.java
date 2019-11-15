
package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String TAG = "MapsActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected FusedLocationProviderClient client;
    int MY_PERMISSIONS_REQUEST_LOCATION;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    Button next;

    ArrayList<Site> sites = new ArrayList<>();
    ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
    LatLngBounds latLngbounce;

    //    FusedLocationProviderClient fusedLocationProviderClient;
    Address address;


    public void fetchSites() {
        db.collection("sites")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Site site = document.toObject(Site.class);
                                sites.add(site);
                            }

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        fetchSites();
        next = findViewById(R.id.nxtbtn);
        Button toSignIn = findViewById(R.id.toSignIn);

        Button createSite = findViewById(R.id.create_site);
        createSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this,CreateSiteActivity.class);
                startActivity(intent);
            }
        });

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
        LatLng origin = new LatLng(40.722543, -73.998585);
        LatLng destination = new LatLng(40.7057, -73.9964);

        drawRoute(origin, destination);


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


    private Address geoLocate(Site site) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(site.getLocation(), 1);
            if (addressList.size() > 0) {
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
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
    }

    public void drawRoute(LatLng origin, LatLng destination) {
        String url = constructUrl(origin, destination);
        new RetrieveDirection().execute(url);
    }

    public String constructUrl(LatLng origin, LatLng destination) {
        String key = "AIzaSyBUlqF7sZtQ3I43i6JG8x3mD7ZSp2AXQlI";
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.latitude + "," + origin.longitude
                + "&destination=" + destination.latitude + "," + destination.longitude + "&key=" + key;
    }

    class RetrieveDirection extends AsyncTask<String, Void, String> {
        private Exception exception;
        String data = "";
        HttpURLConnection connection = null;

        @Override
        protected String doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                //get response
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                data = stringBuilder.toString();
                bufferedReader.close();
                inputStream.close();
                return data;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                connection.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String response) {
            new ParseDirection().execute(response);
        }


    }

    class ParseDirection extends AsyncTask<String, Void, List<List<LatLng>>> {
        @Override
        protected List<List<LatLng>> doInBackground(String... json) {
            List<List<LatLng>> routes = new ArrayList<>();
            try {
                routes = new JSONParser().parseJson(new JSONObject(json[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }
        @Override
        protected void onPostExecute(List<List<LatLng>> result) {
            PolylineOptions polylineOptions = new PolylineOptions();
            try {
                for (List<LatLng> steps : result) {

                    polylineOptions.addAll(steps);
                    polylineOptions.width(3);
                    polylineOptions.color(Color.BLUE);

                }
                mMap.addMarker(new MarkerOptions().position(result.get(0).get(0)).title("Start"));
                mMap.addMarker(new MarkerOptions().position(result.get(result.size() - 1).get((result.get(result.size() - 1)).size() - 1)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(result.get(0).get(0)));
                mMap.addPolyline(polylineOptions);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void getPosition(View view) {

        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(myLocation);

                for (int i = 0; i < sites.size(); i++) {

                    createMarker(geoLocate(sites.get(i)).getLatitude(), geoLocate(sites.get(i)).getLongitude(), sites.get(i).getName());

                    builder.include(new LatLng(geoLocate(sites.get(i)).getLatitude(), geoLocate(sites.get(i)).getLongitude()));

                }
                LatLngBounds bounds = builder.build();

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 30);
                mMap.animateCamera(cameraUpdate);
            }
        });
    }
}