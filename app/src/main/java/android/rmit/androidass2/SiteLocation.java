package android.rmit.androidass2;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class SiteLocation implements ClusterItem {
    private final String title;
    private final String snippet;
    private final LatLng position;

//    public SiteLocation(double lat, double lng) {
//        this.position = new LatLng(lat,lng);
//    }

    public SiteLocation(String title, String snippet, double lat, double lng) {
        this.title = title;
        this.snippet = snippet;
        this.position = new LatLng(lat,lng);
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public String getSnippet() {
        return snippet;
    }

    public String getTitle() {
        return title;
    }
}
