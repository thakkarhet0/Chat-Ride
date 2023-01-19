package com.example.chatroom.models;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

public class Utils {

    public static final String DB_PROFILE = "profiles";
    public static final String DB_CHAT = "chat";
    public static final String DB_CHATROOM = "chatroom";
    public static final String DB_RIDE_OFFER = "ride_offer";
    public static final String DB_RIDE_REQ = "ride_req";
    public static final String DB_TRIPS = "trips";

    public static final String RIDE_TYPE = "Ride";
    public static final String DRIVE_TYPE = "Drive";

    public static final int ID = 0;
    public static final int NAME = 1;
    public static final int PHOTO_REF = 2;

    /**
     * @param date
     * @return
     */
    public static String getPrettyTime(Date date) {
        PrettyTime time = new PrettyTime();
        return time.format(date);
    }

}
