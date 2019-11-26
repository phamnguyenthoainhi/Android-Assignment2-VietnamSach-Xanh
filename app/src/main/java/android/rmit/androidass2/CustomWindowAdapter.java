package android.rmit.androidass2;

import android.content.Context;
import android.rmit.androidass2.R;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.lang.ref.WeakReference;

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
//        String title = marker.getTitle();
        details = view.findViewById(R.id.details);
        campaignname = view.findViewById(R.id.campaignname);

//        if (!title.equals("")) {
//            campaignname.setText(title);
//        }
        campaignname.setText(mapsActivity.clickedLocation.getTitle());


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
