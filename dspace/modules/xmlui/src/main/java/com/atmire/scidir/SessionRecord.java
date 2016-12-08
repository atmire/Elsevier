package com.atmire.scidir;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 06/10/15
 * Time: 10:14
 */
public class SessionRecord {
    String title;
    String authors;
    String eid;

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }
}
