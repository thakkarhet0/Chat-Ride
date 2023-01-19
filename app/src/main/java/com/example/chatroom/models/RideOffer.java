package com.example.chatroom.models;

import java.io.Serializable;
import java.util.ArrayList;

public class RideOffer implements Serializable {

    public ArrayList<String> rider;
    public ArrayList<String> offeror;
    public ArrayList<Double> pickup_location;
    public ArrayList<Double> drop_location;
    String id, msg_id, ride_id, offer_id, pickup_name, drop_name;
    ArrayList<Double> driver_location;

    public RideOffer(String ride_id, ArrayList<String> rider, ArrayList<String> offeror, ArrayList<Double> driver_location, ArrayList<Double> pickup_location, ArrayList<Double> drop_location, String pickup_name, String drop_name) {
        this.ride_id = ride_id;
        this.rider = rider;
        this.offeror = offeror;
        this.driver_location = driver_location;
        this.pickup_location = pickup_location;
        this.drop_location = drop_location;
        this.pickup_name = pickup_name;
        this.drop_name = drop_name;
    }

    public String getPickup_name() {
        return pickup_name;
    }

    public void setPickup_name(String pickup_name) {
        this.pickup_name = pickup_name;
    }

    public String getDrop_name() {
        return drop_name;
    }

    public void setDrop_name(String drop_name) {
        this.drop_name = drop_name;
    }

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Double> getPickup_location() {
        return pickup_location;
    }

    public void setPickup_location(ArrayList<Double> pickup_location) {
        this.pickup_location = pickup_location;
    }

    public ArrayList<Double> getDrop_location() {
        return drop_location;
    }

    public void setDrop_location(ArrayList<Double> drop_location) {
        this.drop_location = drop_location;
    }

    public ArrayList<Double> getDriver_location() {
        return driver_location;
    }

    public void setDriver_location(ArrayList<Double> driver_location) {
        this.driver_location = driver_location;
    }

    public RideOffer() {
    }

    public String getOffer_id() {
        return offer_id;
    }

    public void setOffer_id(String offer_id) {
        this.offer_id = offer_id;
    }

    public ArrayList<String> getRider() {
        return rider;
    }

    public void setRider(ArrayList<String> rider) {
        this.rider = rider;
    }

    public ArrayList<String> getOfferor() {
        return offeror;
    }

    public void setOfferor(ArrayList<String> offeror) {
        this.offeror = offeror;
    }

    public String getRide_id() {
        return ride_id;
    }

    public void setRide_id(String ride_id) {
        this.ride_id = ride_id;
    }

    public String getRiderId() {
        return rider.get(Utils.ID);
    }

    public String getRiderRef() {
        return rider.get(Utils.PHOTO_REF);
    }

    public String getRiderName() {
        return rider.get(Utils.NAME);
    }

    public String getOfferorId() {
        return offeror.get(Utils.ID);
    }

    public String getOfferorRef() {
        return offeror.get(Utils.PHOTO_REF);
    }

    public String getOfferorName() {
        return offeror.get(Utils.NAME);
    }
}
