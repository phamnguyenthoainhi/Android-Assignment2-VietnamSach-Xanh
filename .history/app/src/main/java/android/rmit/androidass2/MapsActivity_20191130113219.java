package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        SearchItemAdapter.SearchItemViewHolder.OnSiteListener {

    private static final String TAG = "MapsActivity";
    private EditText searchbar;
    private RecyclerView searchlist;
    SearchItemAdapter adapter;
    private GoogleMap mMap;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected FusedLocationProviderClient client;
    int MY_PERMISSIONS_REQUEST_LOCATION;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    RecyclerView recyclerView;
    LatLng myLocation;
    LinearLayout mapLayout;
    TextView filterbtn;
    RadioGroup filterradiogroup;
    Polyline polyline;
    ArrayList<Site> sites;
    ArrayList<Site> filtersites;
    ArrayList<Site> afterfiltersites;
    List<String> selectedCities =new ArrayList<>();;
    ClusterManager<SiteLocation> clusterManager;
    SiteLocation clickedLocation;
    List<String> cityList = new ArrayList<>();




    LatLngBounds.Builder builder = new LatLngBounds.Builder();


    private String superuser = "QnZasbpIgNMYpCQ8BIy682YwxS93";
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

        searchbar.setClickable(true);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();






        Log.d(TAG, "onCreate: map"+ currentUser);

        recyclerView = findViewById(R.id.resultsearchlist);
        mapLayout = findViewById(R.id.maplayout);
        recyclerView.setVisibility(View.INVISIBLE);

        Button refreshbtn = findViewById(R.id.refeshbtn);

        fetchSites();
        initRecyclerView();
        filterbtn = findViewById(R.id.filterbtn);
        filterbtn.setClickable(true);

        filterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: hello");
                showdialog(v);
            }
        });


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



        refreshTokenId();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestPermission();
        client = LocationServices.getFusedLocationProviderClient(MapsActivity.this);


        createNavigationBar();

    }




    public class CustomClusterManagerRenderer extends DefaultClusterRenderer<SiteLocation> {
        View marker;
        int dimention;


        IconGenerator iconGenerator;
        IconGenerator clusterIconGenerator;


        @Override
        protected void onBeforeClusterItemRendered(SiteLocation item, MarkerOptions markerOptions) {



            Bitmap icon = iconGenerator.makeIcon();

            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<SiteLocation> cluster) {

            return cluster.getSize() > 5;
>>>>>>> boi
        }

        public CustomClusterManagerRenderer() {
            super(MapsActivity.this, mMap, clusterManager);
            clusterIconGenerator = new IconGenerator(MapsActivity.this);
            iconGenerator = new IconGenerator(MapsActivity.this);


            marker = getLayoutInflater().inflate(R.layout.custom_marker, null);

            clusterIconGenerator.setContentView(marker);
            ImageView imageView = new ImageView(MapsActivity.this);
            imageView.setImageResource(R.drawable.markerplastic);
            dimention = (int) getResources().getDimension(R.dimen.my_value);
            imageView.setPadding(2, 2, 2, 2);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(dimention, dimention));


            iconGenerator.setContentView(imageView);

        }


    }

    public void hideKeyBoard(View view) {

        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.
                getWindowToken(), 0);


    }

    private void refreshTokenId(){
        SharedPreferences sharedPreferences = getSharedPreferences("id",MODE_PRIVATE);
        final String userId = sharedPreferences.getString("uid",null);
        if(userId!=null) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String token = instanceIdResult.getToken();

                    db.collection("Tokens").document(userId).set(new UserToken(token))
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Failed to update token ID");
                                }
                            })
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Updated token ID");
                                }
                            });

                }
            });
        }
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
            public void processFinish(LatLng output, String city) {
                if(output!=null) {
                    Toast.makeText(MapsActivity.this, "Latitude: " + output.latitude + "Longitude: " + output.longitude, Toast.LENGTH_SHORT).show();
                    createMarker(output.latitude, output.longitude, sites.get(i).getName(), sites.get(i).getId());
                    builder.include(output);
                    drawRoute(myLocation, new LatLng(output.latitude, output.longitude));
                }
                else{
                    Toast.makeText(MapsActivity.this, "Could not find route. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getLatLng.execute(constructUrl(sites.get(i).getLocation()));
        recyclerView.setVisibility(View.INVISIBLE);
        hideKeyBoard(recyclerView);

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
                        if (currentUser == null || currentUser.equals("")) {
                            startActivity(new Intent(MapsActivity.this, SignInActivity.class));

                        } else {
                            startActivity(new Intent(MapsActivity.this, SitesActivity.class));
                        }
                        break;
                    case R.id.account_menu:
                        if (currentUser == null|| currentUser.equals("")) {
                            startActivity(new Intent(MapsActivity.this, SignInActivity.class));
                        } else {
                            startActivity(new Intent(MapsActivity.this, ManageAccountActivity.class));

                        }
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
    private void setUpClusterManager(final GoogleMap googleMap){
        clusterManager = new ClusterManager<>(this,googleMap);
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);
        googleMap.setOnInfoWindowClickListener(clusterManager);
        googleMap.setInfoWindowAdapter(clusterManager.getMarkerManager());

        clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<SiteLocation>() {
            @Override
            public void onClusterItemInfoWindowClick(SiteLocation siteLocation) {
                SharedPreferences sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (siteLocation.getSnippet() != null) {
                    editor.putString("sid", siteLocation.getSnippet());
                    editor.commit();
                    if (sharedPreferences.getString("sid", "") != null) {
                        if (currentUser != null) {
//                            Toast.makeText(MapsActivity.this, ""+currentUser.getUid(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MapsActivity.this, SiteDetail.class);
                            intent.putExtra("id",siteLocation.getSnippet());
                            startActivity(intent);
                        } else {
                            startActivity(new Intent(MapsActivity.this, SignInActivity.class));
                        }
                    }
                }
            }
        });

        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<SiteLocation>() {
            @Override
            public boolean onClusterItemClick(SiteLocation siteLocation) {
                clickedLocation = siteLocation;
                return false;
            }
        });

        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<SiteLocation>() {
            @Override
            public boolean onClusterClick(Cluster<SiteLocation> cluster) {
                //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(),(float)Math.floor(googleMap.getCameraPosition().zoom+1)),300,null);
                LatLngBounds.Builder b = LatLngBounds.builder();
                for(SiteLocation siteLocation: cluster.getItems()){
                    b.include(siteLocation.getPosition());
                }
                final LatLngBounds bounds = b.build();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));


                return true;
            }
        });


    }

//    private Address geoLocate(Site site) {
//
//        try {
//            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//            if (site != null) {
//                List<Address> addressList = geocoder.getFromLocationName(site.getLocation(), 1);
//                while (addressList.size() == 0 ) {
//                    addressList = geocoder.getFromLocationName(site.getLocation(), 1);
//                }
//
//                if (addressList.size() > 0) {
//                    address = addressList.get(0);
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return address;
//    }

    public void createMarker(double latitude, double longitude, String title, String id) {
        SiteLocation siteLocation = new SiteLocation(title,id,latitude,longitude);
        clusterManager.addItem(siteLocation);
        clusterManager.setRenderer(new CustomClusterManagerRenderer());

        clusterManager.cluster();
        clusterManager.getMarkerCollection().setOnInfoWindowAdapter(new CustomWindowAdapter(MapsActivity.this));



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);

        mMap.setPadding(0, 0, 30, 0);

        getPosition(MapsActivity.this);
        setUpClusterManager(mMap);


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

//    @Override
//    public void onInfoWindowClick(Marker marker) {
//        SharedPreferences sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        if (marker.getTag() != null) {
//            editor.putString("sid", marker.getTag().toString());
//            editor.commit();
//            if (sharedPreferences.getString("sid", "") != null) {
//                if (currentUser != null) {
//                    Toast.makeText(this, ""+currentUser.getUid(), Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(MapsActivity.this, SiteDetail.class);
//                    intent.putExtra("id",marker.getTag().toString());
//                    startActivity(intent);
//                } else {
//                    startActivity(new Intent(MapsActivity.this, SignInActivity.class));
//                }
//            }
//        }
//    }


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
                LatLngBounds.Builder b = LatLngBounds.builder();
                b.include(result.get(0).get(0));
                b.include(result.get(result.size()-1).get((result.get(result.size()-1)).size()-1));
                final LatLngBounds bounds = b.build();
                activity.mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));
                activity.polyline =  activity.mMap.addPolyline(polylineOptions);

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }



    public void addMarkers() {
        cityList = new ArrayList<>();
        for (final Site site: sites) {

            GetLatLng getLatLng = new GetLatLng(new GetLatLng.AsyncResponse() {
                @Override
                public void processFinish(LatLng output, String city) {
                    cityList.add(city);

                    createMarker(output.latitude,output.longitude,site.getName(),site.getId());

                    builder.include(output);
                    if(sites.indexOf(site)==sites.size()-1) {

                        LatLngBounds bounds = builder.build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 180);
                        mMap.animateCamera(cameraUpdate);
                    }


                }
            });
            getLatLng.execute(constructUrl(site.getLocation()));

        }

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

    public void fetchSites2() {
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
                                filtersites.add(site);
                            }

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }




    public void showdialog(View v) {
        filtersites = new ArrayList<>();
        afterfiltersites = new ArrayList<>();

        final AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
        final View dialog = getLayoutInflater().inflate(R.layout.filter_layout, null);

        alert.setView(dialog);
        final AlertDialog alertDialog = alert.create();

        final List<View> checkBox = new ArrayList<>();
        List<String> converted = new ArrayList<>();
        List<String> compare = new ArrayList<>();
        String search = "City";
        for(String original:cityList){
            if(!compare.contains(convert(original).toLowerCase().replaceAll("\\s",""))) {
                if (original.toLowerCase().contains(search.toLowerCase())) {
                    converted.add(convert(original.replace(search, "").trim()));
                    compare.add(convert(original.replace(search, "").trim()).toLowerCase().replaceAll("\\s", ""));

                } else {
                    compare.add(convert(original).toLowerCase().replaceAll("\\s", ""));
                    converted.add(convert(original));
                }
            }
        }

        Set<String> filtered = new HashSet<>(converted);
        converted.clear();
        converted.addAll(filtered);

        LinearLayout ll = dialog.findViewById(R.id.checkbox_layout);

        for(String city: converted){
            //Toast.makeText(this, city, Toast.LENGTH_SHORT).show();
            View cb = getLayoutInflater().inflate(R.layout.filter_checkbox,null);
            ((CheckBox)cb).setText(city);
            if(selectedCities.contains(city.toLowerCase().replaceAll("\\s",""))){
                ((CheckBox)cb).setChecked(true);
            }
            checkBox.add(cb);

            ll.addView(cb);
        }

        alertDialog.setCanceledOnTouchOutside(true);


        filterradiogroup = dialog.findViewById(R.id.filterRadioGroup);
        Button apply = dialog.findViewById(R.id.apply);

        fetchSites2();
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(View view: checkBox){
                    if(((CheckBox)view).isChecked()){
                        if(!selectedCities.contains(((CheckBox)view).getText().toString().toLowerCase().replaceAll("\\s",""))) {
                            selectedCities.add(((CheckBox) view).getText().toString().toLowerCase().replaceAll("\\s", ""));
                        }
                    }
                    else {
                        if(selectedCities.contains(((CheckBox)view).getText().toString().toLowerCase().replaceAll("\\s",""))){
                            selectedCities.remove(((CheckBox)view).getText().toString().toLowerCase().replaceAll("\\s",""));
                        }
                    }
                }

                alertDialog.dismiss();
                addMarkersByFilter();


            }

        });

        filterradiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedbutton = filterradiogroup.findViewById(checkedId);
                boolean isChecked = checkedbutton.isChecked();



                if (isChecked && checkedbutton.getText().equals("6AM - 12AM")) {


                    for (int i = 0; i < filtersites.size(); i++) {
                        if (convertDate(filtersites.get(i).getDateTime()) <= 12 && convertDate(filtersites.get(i).getDateTime()) >= 6) {
                            afterfiltersites.add(filtersites.get(i));
                            Log.d(TAG, "onCheckedChanged: 6"+ afterfiltersites);
                        }
                    }
                }
                if (isChecked && checkedbutton.getText().equals("1PM - 6PM")) {


                    for (int i = 0; i < filtersites.size(); i++) {
                        if (convertDate(filtersites.get(i).getDateTime()) >= 13 && convertDate(filtersites.get(i).getDateTime()) <= 18) {
                            afterfiltersites.add(filtersites.get(i));
                            Log.d(TAG, "onCheckedChanged: 1"+ afterfiltersites);

                        }
                    }
                }
            }
        });
        alert.show();

    }
    public int convertDate(long millsec) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(millsec);

        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        return mHour;
    }

    public void addMarkersByFilter() {
        mMap.clear();
        clusterManager.clearItems();

        Toast.makeText(this, selectedCities.toString(), Toast.LENGTH_SHORT).show();
        if (selectedCities.size() == 0 && afterfiltersites.size() > 0) {

            for (final Site site : afterfiltersites) {
                GetLatLng getLatLng = new GetLatLng(new GetLatLng.AsyncResponse() {
                    @Override
                    public void processFinish(LatLng output, String city) {
                        createMarker(output.latitude, output.longitude, site.getName(), site.getId());
                        builder.include(output);
                        if(afterfiltersites.indexOf(site)==afterfiltersites.size()-1) {

                            LatLngBounds bounds = builder.build();
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                            mMap.animateCamera(cameraUpdate);
                        }

                    }
                });
                getLatLng.execute(constructUrl(site.getLocation()));

            }
        }

        else if (afterfiltersites.size() == 0 &&  selectedCities.size() > 0 ) {

            for (final Site site : sites) {
                GetLatLng getLatLng = new GetLatLng(new GetLatLng.AsyncResponse() {
                    @Override
                    public void processFinish(LatLng output, String city) {
                        String current = convert(city).toLowerCase().replaceAll("\\s", "");
                        if (selectedCities.contains(current) ) {

                            createMarker(output.latitude, output.longitude, site.getName(), site.getId());
                            builder.include(output);

                        }
                        if(sites.indexOf(site)==sites.size()-1) {

                            LatLngBounds bounds = builder.build();
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                            mMap.animateCamera(cameraUpdate);
                        }
                    }
                });
                getLatLng.execute(constructUrl(site.getLocation()));

            }

        }

        else if (selectedCities.size() > 0  &&  afterfiltersites.size() > 0) {

            for (final Site site : sites) {
                GetLatLng getLatLng = new GetLatLng(new GetLatLng.AsyncResponse() {
                    @Override
                    public void processFinish(LatLng output, String city) {
                        String current = convert(city).toLowerCase().replaceAll("\\s", "");
                        for (Site after:afterfiltersites) {
                            if (selectedCities.contains(current) && after.getId().equalsIgnoreCase(site.getId())) {
                                createMarker(output.latitude, output.longitude, site.getName(), site.getId());
                                builder.include(output);


                            }
                            if(afterfiltersites.indexOf(after)==afterfiltersites.size()-1) {

                                LatLngBounds bounds = builder.build();
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                                mMap.animateCamera(cameraUpdate);
                            }
                        }
                    }
                });
                getLatLng.execute(constructUrl(site.getLocation()));

            }
        }

        else if(selectedCities.size()==0 && afterfiltersites.size()==0){
            addMarkers();
        }


    }

    public String convert(String vietnamese){
        try{
            String temp = Normalizer.normalize(vietnamese,Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            System.out.println(pattern.matcher(temp).replaceAll(""));
            return pattern.matcher(temp).replaceAll("").replace("đ","d").replace('Đ','D');
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;

    }


    public class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {
        TextView details;
        private View window;
        private Context context;
        TextView campaignname;
        MapsActivity mapsActivity;

        public CustomWindowAdapter(final Context context) {
            this.context = context;
            window = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);


        }
        private void renderWindowButton(Marker marker, View view) {
            details = view.findViewById(R.id.details);
            campaignname = view.findViewById(R.id.campaignname);

            campaignname.setText(clickedLocation.getTitle());


        }


        @Override
        public View getInfoWindow(Marker marker) {
            renderWindowButton(marker, window);
            return window;
        }

        @Override
        public View getInfoContents(Marker marker) {
            renderWindowButton(marker, window);

            return window;
        }

    }


}