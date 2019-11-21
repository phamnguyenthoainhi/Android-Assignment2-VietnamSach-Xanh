package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, SearchItemAdapter.SearchItemViewHolder.OnSiteListener {

    private static final String TAG = "MapsActivity";
    private EditText searchbar;
    private RecyclerView searchlist;
    SearchItemAdapter adapter;
    private GoogleMap mMap;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected FusedLocationProviderClient client;
    int MY_PERMISSIONS_REQUEST_LOCATION;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    RecyclerView recyclerView;
    LatLng myLocation;
    LinearLayout mapLayout;

    ArrayList<Site> sites;
    ArrayList<Site> searchsites;
    ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
    LatLngBounds latLngbounce;
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    private LatLngBounds.Builder bounds2;
    //    FusedLocationProviderClient fusedLocationProviderClient;
    Address address;

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        sites = new ArrayList<>();
        searchbar = findViewById(R.id.searchbar);
        searchlist = findViewById(R.id.resultsearchlist);
        searchbar.setClickable(true);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        recyclerView = findViewById(R.id.resultsearchlist);
        mapLayout = findViewById(R.id.maplayout);
        recyclerView.setVisibility(View.INVISIBLE);

//        Button todetails = findViewById(R.id.todetails);
        Button refreshbtn = findViewById(R.id.refeshbtn);

        fetchSites();
        initRecyclerView();


        searchbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);

            }
        });

        mapLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                recyclerView.setVisibility(View.INVISIBLE);
                hideKeyBoard(mapLayout);
                return  false;
            }
        });


        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: "+ s);
                adapter.filter(s, sites);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        refreshbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sites = new ArrayList<>();
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
        createNavigationBar();

    }

    public void hideKeyBoard(View view) {

        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.
                getWindowToken(), 0);


    }


    public void initRecyclerView() {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);
        adapter = new SearchItemAdapter(sites, MapsActivity.this);

        recyclerView.setLayoutManager(layoutManager);


        recyclerView.setAdapter(adapter);

    }


    @Override
    public void onSiteClick(int position) {
        final int i = position;
        GetLatLng getLatLng = new GetLatLng(new GetLatLng.AsyncResponse() {
            @Override
            public void processFinish(LatLng output) {
                Toast.makeText(MapsActivity.this, "Latitude: " + output.latitude + "Longitude: "+output.longitude, Toast.LENGTH_SHORT).show();
                createMarker(output.latitude,output.longitude,sites.get(i).getName(),sites.get(i).getId());
                builder.include(output);
                drawRoute(myLocation, new LatLng(output.latitude, output.longitude));

            }
        });

        getLatLng.execute(constructUrl(sites.get(i).getLocation()));


    }




    public void createNavigationBar() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                System.out.println("onNavigationItemSelected: currentuser" + currentUser);
                Log.d(TAG, "onNavigationItemSelected: currentuser" + (currentUser == null));
                switch (menuItem.getItemId()) {
                    case R.id.home_menu:
                        menuItem.setChecked(true);
                        break;
                    case R.id.site_menu:
                        System.out.println(Log.d(TAG, "onNavigationItemSelected: currentuser" + (currentUser == null)));
                        if (currentUser == null) {
                            System.out.println("hello yes");
                            startActivity(new Intent(MapsActivity.this, SignInActivity.class));

                        } else {
                            System.out.println("hello no");
                            startActivity(new Intent(MapsActivity.this, SitesActivity.class));
                        }
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
        Log.d(TAG, "fetchSites: called");
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
                while (addressList.size() == 0 ) {
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
        getPosition(MapsActivity.this);


    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MapsActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
    }

    public void drawRoute(LatLng origin, LatLng destination) {
        String url = constructUrl(origin, destination);
        new RetrieveDirection(this).execute(url);
    }

    public String constructUrl(LatLng origin, LatLng destination) {
        String key = getString(R.string.google_maps_key);
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.latitude + "," + origin.longitude
                + "&destination=" + destination.latitude + "," + destination.longitude + "&key=" + key;
    }

    public String constructUrl(String location){
        String key = getString(R.string.google_maps_key);
        return "https://maps.googleapis.com/maps/api/geocode/json?address="+location.replaceAll("\\s","+")+"&key="+key;
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
                    startActivity(new Intent(MapsActivity.this, SignInActivity.class));
                }
            }
        }
    }


    private static class RetrieveDirection extends AsyncTask<String, Void, String> {
        private Exception exception;
        String data = "";
        HttpURLConnection connection = null;

        private WeakReference<MapsActivity> weakReference;

        RetrieveDirection(MapsActivity context){
            weakReference = new WeakReference<>(context);
        }

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
            new ParseDirection(weakReference.get()).execute(response);

        }

    }


    private static class ParseDirection extends AsyncTask<String, Void, List<List<LatLng>>> {

        private WeakReference<MapsActivity> weakReference;

        ParseDirection(MapsActivity context){
            weakReference = new WeakReference<>(context);
        }

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
                MapsActivity activity = weakReference.get();
                activity.mMap.moveCamera(CameraUpdateFactory.newLatLng(result.get(0).get(0)));
                activity.mMap.addPolyline(polylineOptions);

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }



    public void addMarkers() {
        for (final Site site: sites) {
//            createMarker(geoLocate(sites.get(i)).getLatitude(), geoLocate(sites.get(i)).getLongitude(), sites.get(i).getName(), sites.get(i).getId());
//            createMarker(geoLocate(sites.get(i)).getLatitude(), geoLocate(sites.get(i)).getLongitude(), sites.get(i).getName(), sites.get(i).getId());
            GetLatLng getLatLng = new GetLatLng(new GetLatLng.AsyncResponse() {
                @Override
                public void processFinish(LatLng output) {
                    //Toast.makeText(MapsActivity.this, "Latitude: " + output.latitude + "Longitude: "+output.longitude, Toast.LENGTH_SHORT).show();
                    createMarker(output.latitude,output.longitude,site.getName(),site.getId());
                    builder.include(output);
                    LatLngBounds bounds = builder.build();
                    mMap.setInfoWindowAdapter(new CustomWindowAdapter(MapsActivity.this));
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 180);
                    mMap.animateCamera(cameraUpdate);

                }
            });
            getLatLng.execute(constructUrl(site.getLocation()));

        }
//        Log.d(TAG, "addMarkers: buidlder " + builder.toString());
//        builder.include(new LatLng(10.776080, 106.703040));
//        LatLngBounds bounds = builder.build();
//
//        mMap.setInfoWindowAdapter(new CustomWindowAdapter(MapsActivity.this));
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 80);
//        mMap.animateCamera(cameraUpdate);
    }

    @SuppressLint("MissingPermission")
    public void getPosition(Context context) {
        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {


                myLocation = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));


                builder.include(myLocation);

            }
        });
    }
}