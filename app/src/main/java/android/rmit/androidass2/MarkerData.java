package android.rmit.androidass2;

public class MarkerData {
//    double latitude;
//    double longitude;
    String title;
    String snippet;
    String address;

    public MarkerData(String address) {
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "MarkerData{" +
                "title='" + title + '\'' +
                ", snippet='" + snippet + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
