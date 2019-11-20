package android.rmit.androidass2;

import android.os.AsyncTask;


import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
public class GetLatLngList extends AsyncTask<List<String>, Void, List<LatLng>> {
    public interface Response{
        void processFinish(List<LatLng> output);


    }
    public Response delegate = null;
    private String key = "AIzaSyBUlqF7sZtQ3I43i6JG8x3mD7ZSp2AXQlI";

    public String constructUrl(String location){
        return "https://maps.googleapis.com/maps/api/geocode/json?address="+location.replaceAll("\\s","+")+"&key="+key;
    }
    public GetLatLngList(Response delegate){
        this.delegate = delegate;
    }
    @Override
    protected List<LatLng> doInBackground(List<String>... lists) {
        try {
            List<String> sites = lists[0];
            final List<LatLng> coords = new ArrayList<>();

            for (String site : sites) {
                GetLatLng getLatLng = new GetLatLng(new GetLatLng.AsyncResponse() {
                    @Override
                    public void processFinish(LatLng output) {
                        //Toast.makeText(MapsActivity.this, "Latitude: " + output.latitude + "Longitude: "+output.longitude, Toast.LENGTH_SHORT).show();
                        //createMarker(output.latitude,output.longitude,site.getName(),site.getId());
                        coords.add(output);

                    }
                });
                getLatLng.execute(constructUrl(site));

            }

            return coords;
        }catch(Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<LatLng> latLngs) {
        super.onPostExecute(latLngs);
        delegate.processFinish(latLngs);

    }
}
