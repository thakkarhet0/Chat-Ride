package com.example.chatroom.models;

public class Viewer {

    public String uid, photoRef, name;

    public Viewer() {
    }

    public Viewer(String uid, String photoRef, String name) {
        this.uid = uid;
        this.photoRef = photoRef;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhotoRef() {
        return photoRef;
    }

    public void setPhotoRef(String photoRef) {
        this.photoRef = photoRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
