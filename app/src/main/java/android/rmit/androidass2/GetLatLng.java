package android.rmit.androidass2;


import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetLatLng extends AsyncTask<String, Void, String> {
    String data = "";
    HttpURLConnection connection = null;
    public interface AsyncResponse{
        void processFinish(LatLng output, String city);


    }
    public AsyncResponse delegate = null;
    public GetLatLng (AsyncResponse delegate){
        this.delegate=delegate;
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
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = ((JSONArray)jsonObject.get("results"));
            JSONObject object = jsonArray.getJSONObject(0);
            JSONArray components = object.getJSONArray("address_components");
            double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");

            double lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");

            String city="";

            for(int i = 0;i<components.length();i++){
                JSONObject component = components.getJSONObject(i);
                System.out.print(components);
                if (!city.equals("")){
                    break;
                }
                for(int k = 0;k<component.getJSONArray("types").length();k++){
                    if(component.getJSONArray("types").get(k).equals("locality")){
                        city = component.getString("long_name");
                        break;
                    }
                }

            }

            if(city.equals("")){
                for(int i = 0;i<components.length();i++){
                    JSONObject component = components.getJSONObject(i);
                    System.out.print(components);
                    if (!city.equals("")){
                        break;
                    }
                    for(int k = 0;k<component.getJSONArray("types").length();k++){
                        if(component.getJSONArray("types").get(k).equals("administrative_area_level_1")){
                            city = component.getString("long_name");
                            break;
                        }
                    }

                }

            }



            Log.d("latitude", "" + lat);
            Log.d("longitude", "" + lng);


            delegate.processFinish(new LatLng(lat,lng), city);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}