package android.rmit.androidass2;


public class UserNotification {


    String content;
    String type;
    String siteId;
    String from;
    String to;
    String siteName;

    public UserNotification() {
    }

    public UserNotification(String content, String type, String siteId, String from, String to, String siteName) {
        this.content = content;
        this.type = type;
        this.siteId = siteId;
        this.from = from;
        this.to = to;
        this.siteName = siteName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
