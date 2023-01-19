package com.example.chatroom.models;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Trip implements Serializable {

    String id;

    int number;
    boolean ongoing = true;
    String ride_id, msg_id, pickup_name, drop_name;
    Date started_at;
    ArrayList<String> rider;
    ArrayList<String> driver;
    ArrayList<Double> rider_location;
    ArrayList<Double> driver_location;
    ArrayList<Double> drop_location;
    Float driver_bearing = null;
    Date end_at;

    public Trip(String ride_id, Date started_at, ArrayList<String> rider, ArrayList<String> driver, ArrayList<Double> rider_location, ArrayList<Double> driver_location, ArrayList<Double> drop_location, String pickup_name, String drop_name) {
        this.ride_id = ride_id;
        this.started_at = started_at;
        this.rider = rider;
        this.driver = driver;
        this.rider_location = rider_location;
        this.driver_location = driver_location;
        this.drop_location = drop_location;
        this.pickup_name = pickup_name;
        this.drop_name = drop_name;
    }

    public Float getDriver_bearing() {
        return driver_bearing;
    }

    public void setDriver_bearing(Float driver_bearing) {
        this.driver_bearing = driver_bearing;
    }

    public Date getEnd_at() {
        return end_at;
    }

    public void setEnd_at(Date end_at) {
        this.end_at = end_at;
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

    public LatLng getDriverLatLng() {
        return new LatLng(driver_location.get(0), driver_location.get(1));
    }

    public LatLng getRiderLatLng() {
        return new LatLng(rider_location.get(0), rider_location.get(1));
    }

    public LatLng getDropLatLng() {
        return new LatLng(drop_location.get(0), drop_location.get(1));
    }

    public Trip() {
    }

    public String getStatus() {
        return (ongoing) ? "Ongoing" : "Completed";
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isOngoing() {
        return ongoing;
    }

    public void setOngoing(boolean ongoing) {
        this.ongoing = ongoing;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMsg_id() {
        return msg_id;
    }

    public ArrayList<Double> getDrop_location() {
        return drop_location;
    }

    public void setDrop_location(ArrayList<Double> drop_location) {
        this.drop_location = drop_location;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    public ArrayList<Double> getDriver_location() {
        return driver_location;
    }

    public void setDriver_location(ArrayList<Double> driver_location) {
        this.driver_location = driver_location;
    }

    public Date getStarted_at() {
        return started_at;
    }

    public void setStarted_at(Date started_at) {
        this.started_at = started_at;
    }

    public String getRide_id() {
        return ride_id;
    }

    public void setRide_id(String ride_id) {
        this.ride_id = ride_id;
    }

    public ArrayList<String> getRider() {
        return rider;
    }

    public void setRider(ArrayList<String> rider) {
        this.rider = rider;
    }

    public ArrayList<String> getDriver() {
        return driver;
    }

    public void setDriver(ArrayList<String> driver) {
        this.driver = driver;
    }

    public ArrayList<Double> getRider_location() {
        return rider_location;
    }

    public void setRider_location(ArrayList<Double> rider_location) {
        this.rider_location = rider_location;
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

    public String getDriverId() {
        return driver.get(Utils.ID);
    }

    public String getDriverRef() {
        return driver.get(Utils.PHOTO_REF);
    }

    public String getDriverName() {
        return driver.get(Utils.NAME);
    }

}
