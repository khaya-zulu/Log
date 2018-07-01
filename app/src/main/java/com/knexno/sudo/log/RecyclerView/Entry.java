package com.knexno.sudo.log.RecyclerView;

public class Entry {

    private String uid, heading, body, date;

    public Entry(String uid, String heading, String body, String date) {
        this.uid = uid;
        this.heading = heading;
        this.body = body;
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
