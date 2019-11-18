package android.rmit.androidass2;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {
    TextView details;
    private View window;
    private Context context;
    TextView campaignname;

    public CustomWindowAdapter(final Context context) {
        this.context = context;
        window = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);


    }
    private void renderWindowButton(Marker marker, View view) {
        String title = marker.getTitle();
        details = view.findViewById(R.id.details);
        campaignname = view.findViewById(R.id.campaignname);

        if (!title.equals("")) {
            campaignname.setText(title);
        }


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
