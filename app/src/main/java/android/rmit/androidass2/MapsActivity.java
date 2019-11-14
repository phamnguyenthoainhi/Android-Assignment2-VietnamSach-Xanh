package android.rmit.androidass2;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LatLng origin = new LatLng(40.722543,-73.998585);
        LatLng destination = new LatLng(40.7057,-73.9964);

        //Toast.makeText(this, constructUrl(origin,destination), Toast.LENGTH_SHORT).show();

        //String url = "https://maps.googleapis.com/maps/api/directions/json?origin=Disneyland&destination=Universal+Studios+Hollywood&key=AIzaSyBUlqF7sZtQ3I43i6JG8x3mD7ZSp2AXQlI";
//        String url = constructUrl(origin,destination);
//        new RetrieveDirection().execute(url);
        drawRoute(origin,destination);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent intent = new Intent(MapsActivity.this,CreateSiteActivity.class);
                intent.putExtra("coord",latLng);

                startActivity(intent);
            }
        });
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

    }

    public void drawRoute(LatLng origin,LatLng destination){
        String url = constructUrl(origin,destination);
        new RetrieveDirection().execute(url);
    }

    public String constructUrl(LatLng origin, LatLng destination){
        String key = "AIzaSyBUlqF7sZtQ3I43i6JG8x3mD7ZSp2AXQlI";
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.latitude+","+origin.longitude
                +"&destination="+destination.latitude+","+destination.longitude+"&key="+key;
    }


    class RetrieveDirection extends AsyncTask<String, Void, String>{
        private Exception exception;
        String data = "";
        HttpURLConnection connection=null;

        @Override
        protected String doInBackground(String... urls){

                try{
                    URL url = new URL(urls[0]);
                    connection = (HttpURLConnection)url.openConnection();
                    connection.connect();


                    //get response
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line="";
                    while((line = bufferedReader.readLine())!=null){
                        stringBuilder.append(line);
                    }

                    data = stringBuilder.toString();
                    bufferedReader.close();
                    inputStream.close();
                    return data;

                }catch (Exception e){
                    e.printStackTrace();
                    return null;
                }finally{
                    connection.disconnect();
                }
            }
            @Override
            protected void onPostExecute(String response){
                Toast.makeText(MapsActivity.this, response.substring(0,10), Toast.LENGTH_SHORT).show();
                new ParseDirection().execute(response);
            }


    }
    class ParseDirection extends AsyncTask<String,Void,List<List<LatLng>>>{
        @Override
        protected List<List<LatLng>> doInBackground(String... json){
            List<List<LatLng>> routes = new ArrayList<>();
            try{
                routes = new JSONParser().parseJson(new JSONObject(json[0]));

            }
            catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<LatLng>> result){
            PolylineOptions polylineOptions= new PolylineOptions();
            try{
                for(List<LatLng> steps:result){

                    polylineOptions.addAll(steps);
                    polylineOptions.width(3);
                    polylineOptions.color(Color.BLUE);


                }
                mMap.addMarker(new MarkerOptions().position(result.get(0).get(0)).title("Start"));
                mMap.addMarker(new MarkerOptions().position(result.get(result.size()-1).get((result.get(result.size()-1)).size()-1)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(result.get(0).get(0)));
                mMap.addPolyline(polylineOptions);}
            catch(NullPointerException e){
                e.printStackTrace();
            }
        }
    }
}
