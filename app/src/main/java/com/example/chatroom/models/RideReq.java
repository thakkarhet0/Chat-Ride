package com.example.chatroom.models;

import java.io.Serializable;
import java.util.ArrayList;

public class RideReq implements Serializable {

    public ArrayList<Double> pickup_location;
    public ArrayList<Double> drop_location;

    public ArrayList<String> offer_ids = new ArrayList<>();

    String pickup_name, drop_name;
    public String id, msg_id;
    public ArrayList<String> requester;
    public ArrayList<String> offer_msgs;

    public RideReq(ArrayList<Double> pickup, ArrayList<Double> drop, ArrayList<String> requester, ArrayList<String> offer_msgs, String pickup_name, String drop_name) {
        this.pickup_location = pickup;
        this.drop_location = drop;
        this.requester = requester;
        this.offer_msgs = offer_msgs;
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

    public void addOffer(String id) {
        if (!this.offer_ids.contains(id)) this.offer_ids.add(id);
    }

    public ArrayList<String> getOffer_ids() {
        return offer_ids;
    }

    public void setOffer_ids(ArrayList<String> offer_ids) {
        this.offer_ids = offer_ids;
    }

    public RideReq() {
    }

    public ArrayList<String> getRequester() {
        return requester;
    }

    public void setRequester(ArrayList<String> requester) {
        this.requester = requester;
    }

    public String getRequesterId() {
        return requester.get(Utils.ID);
    }

    public String getRequesterRef() {
        return requester.get(Utils.PHOTO_REF);
    }

    public String getRequesterName() {
        return requester.get(Utils.NAME);
    }

    public ArrayList<String> getOffer_msgs() {
        return offer_msgs;
    }

    public void setOffer_msgs(ArrayList<String> offer_msgs) {
        this.offer_msgs = offer_msgs;
    }

    public void addOfferMsg(String uid) {
        this.offer_msgs.add(uid);
    }

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
