package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,
        SiteAdapter.SiteViewHolder.OnSiteListener, SearchItemAdapter.SearchItemViewHolder.OnSiteListener {

    private EditText searchbar;
    private RecyclerView searchlist;
    SearchItemAdapter adapter;
    private GoogleMap mMap;
    String TAG = "MapsActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected FusedLocationProviderClient client;
    int MY_PERMISSIONS_REQUEST_LOCATION;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    ArrayList<Site> sites = new ArrayList<>();
    ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
    LatLngBounds latLngbounce;
    LatLngBounds.Builder builder;

    //    FusedLocationProviderClient fusedLocationProviderClient;
    Address address;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        searchbar = findViewById(R.id.searchbar);
        searchlist = findViewById(R.id.resultsearchlist);

        searchbar.setClickable(true);
        adapter = new SearchItemAdapter(sites, this);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        Button todetails = findViewById(R.id.todetails);

        todetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, ManageSiteActivity.class));
            }

        });

        searchbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initRecyclerView();
            }
        });


        Button createSite = findViewById(R.id.create_site);
        createSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapsActivity.this,CreateSiteActivity.class));
            }
        });


        Button refreshbtn = findViewById(R.id.refeshbtn);
        refreshbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchSites();
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

//        drawRoute(origin, destination);
        fetchSites();
        getPosition(MapsActivity.this);
        createNavigationBar();


        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
//            doMySearch(query);
        }

    }

    
    public void searchLocation() {
//        FirestoreAdapter<SearchItemsViewHolder> = new FirestoreAdapter<SearchItemsViewHolder>()
    }

    public void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.resultsearchlist);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);


        recyclerView.setLayoutManager(layoutManager);


        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onSiteClick(int position) {
        Toast.makeText(this, ""+position, Toast.LENGTH_SHORT).show();

    }





    public void createNavigationBar() {

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.home_menu:
                        menuItem.setChecked(true);
                        break;
                    case R.id.site_menu:
                        startActivity(new Intent(MapsActivity.this, SitesActivity.class));
                        break;
                    case R.id.account_menu:
                        startActivity(new Intent(MapsActivity.this, ManageAccountActivity.class));
                        break;
                }
                return true;
            }
        });
    }

    public void fetchSites() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        db.collection("Sites")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Site site = document.toObject(Site.class);
                                site.setId(document.getId());
                                sites.add(site);
                            }
                            adapter.notifyDataSetChanged();
                            addMarkers();

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private Address geoLocate(Site site) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            if (site != null) {
                List<Address> addressList = geocoder.getFromLocationName(site.getLocation(), 1);
                Toast.makeText(this, site.getLocation(), Toast.LENGTH_SHORT).show();
                while(addressList.size()==0){
                    addressList = geocoder.getFromLocationName(site.getLocation(), 1);
                }
                if (addressList.size() > 0) {
                    address = addressList.get(0);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }

    public Marker createMarker(double latitude, double longitude, String title, String id) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title)
        );
        marker.setTag(id);
        return marker;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);

        mMap.setPadding(0, 0, 30, 0);
        mMap.setOnInfoWindowClickListener(this);
        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(myLocation);


            }
        });

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


    @Override
    public void onInfoWindowClick(Marker marker) {
        SharedPreferences sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (marker.getTag() != null) {
            editor.putString("sid", marker.getTag().toString());
            editor.commit();
            if (sharedPreferences.getString("sid", "") != null) {
                if (currentUser != null) {
                    Toast.makeText(this, ""+currentUser.getUid(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MapsActivity.this, SiteDetail.class);
                    intent.putExtra("id",marker.getTag().toString());
                    startActivity(intent);

                } else {
                    Toast.makeText(this, ""+currentUser.getUid(), Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(MapsActivity.this, SignInActivity.class));
                }
            }
        }
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

    @Override
    protected void onStart() {
        super.onStart();


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

    public void addMarkers() {
//        for (int i = 0; i < sites.size(); i++) {
//            createMarker(geoLocate(sites.get(i)).getLatitude(), geoLocate(sites.get(i)).getLongitude(), sites.get(i).getName(), sites.get(i).getId());
//            createMarker(geoLocate(sites.get(i)).getLatitude(), geoLocate(sites.get(i)).getLongitude(), sites.get(i).getName(), sites.get(i).getId());
//            builder.include(new LatLng(geoLocate(sites.get(i)).getLatitude(), geoLocate(sites.get(i)).getLongitude()));
//        }

        LatLngBounds bounds = builder.build();
        mMap.setInfoWindowAdapter(new CustomWindowAdapter(MapsActivity.this));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 80);
        mMap.animateCamera(cameraUpdate);
    }

    @SuppressLint("MissingPermission")
    public void getPosition(Context context) {
        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));

                builder = new LatLngBounds.Builder();
                builder.include(myLocation);

            }
        });
    }
}