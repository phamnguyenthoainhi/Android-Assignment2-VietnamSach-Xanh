package android.rmit.androidass2;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JSONParser {

    public List<List<LatLng>> parseJson(JSONObject direction){
        List<List<LatLng>> routes = new ArrayList<>();

        try{
            JSONArray jsonRoutes = direction.getJSONArray("routes");
            for (int i = 0;i<jsonRoutes.length();i++){
                List<LatLng> steps = new ArrayList<>();
                JSONArray jsonLegs = ((JSONObject)jsonRoutes.get(i)).getJSONArray("legs");
                for (int j=0;j<jsonLegs.length();j++){

                    JSONArray jsonSteps =  ((JSONObject)jsonLegs.get(j)).getJSONArray("steps");

                    for (int k = 0;k<jsonSteps.length();k++){
                        String polyline = (String)((JSONObject)((JSONObject)jsonSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> points = PolyUtil.decode(polyline);
                        for(LatLng point: points){
                            steps.add(point);
                        }

                        }
                    routes.add(steps);
                    }
                }

        } catch(JSONException e){
            e.printStackTrace();
        } catch(Exception e){


        }

        return routes;
    }


}
