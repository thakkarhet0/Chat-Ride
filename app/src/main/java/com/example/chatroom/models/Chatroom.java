package com.example.chatroom.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Chatroom implements Serializable {

    public String name;
    public String id;
    public int number;
    public String created_by;
    public Date created_at;
    public HashMap<String, Viewer> viewers = new HashMap<>();

    public HashMap<String, Viewer> getViewers() {
        return viewers;
    }

    public void setViewers(HashMap<String, Viewer> viewers) {
        this.viewers = viewers;
    }

    public Chatroom() {
    }

    public void addViewer(String uid, Viewer viewer) {
        this.viewers.put(uid, viewer);
    }

    public void removeViewer(String uid) {
        this.viewers.remove(uid);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public Date getCreated_at() {
        return (created_at == null ? new Date() : created_at);
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
