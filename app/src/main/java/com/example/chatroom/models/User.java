package com.example.chatroom.models;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {

    String firstname, lastname, photoref, city, email, gender, id;

    ArrayList<String> rides = new ArrayList<>();

    Chatroom chatroom;

    RideOffer rideOffer;

    RideReq rideReq;

    public boolean ride_offer = false;
    public boolean ride_req = false;
    public boolean ride_started = false;
    public boolean ride_finished = false;

    Trip trip;

    @Override
    public String toString() {
        return "User{" +
                "firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", photoref='" + photoref + '\'' +
                ", city='" + city + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", id='" + id + '\'' +
                ", rides=" + rides +
                ", chatroom=" + chatroom +
                ", rideOffer=" + rideOffer +
                ", rideReq=" + rideReq +
                ", trip=" + trip +
                '}';
    }

    public ArrayList<String> getRides() {
        return rides;
    }

    public void setRides(ArrayList<String> rides) {
        this.rides = rides;
    }

    public void addRide(String ride) {
        if (!this.rides.contains(ride))
            this.rides.add(ride);
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public RideOffer getRideOffer() {
        return rideOffer;
    }

    public void setRideOffer(RideOffer rideOffer) {
        this.rideOffer = rideOffer;
    }

    public RideReq getRideReq() {
        return rideReq;
    }

    public void setRideReq(RideReq rideReq) {
        this.rideReq = rideReq;
    }

    public Chatroom getChatroom() {
        return chatroom;
    }

    public void setChatroom(Chatroom chatroom) {
        this.chatroom = chatroom;
    }

    public User() {
    }

    public User(String firstname, String lastname, String photoref, String city, String email, String gender, String id) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.photoref = photoref;
        this.city = city;
        this.email = email;
        this.gender = gender;
        this.id = id;
    }

    public String getDisplayName() {
        return this.firstname + " " + this.lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhotoref() {
        return photoref;
    }

    public void setPhotoref(String photoref) {
        this.photoref = photoref;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
