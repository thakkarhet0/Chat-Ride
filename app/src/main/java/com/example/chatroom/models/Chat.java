package com.example.chatroom.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Chat implements Serializable {

    public static final int CHAT_MESSAGE = 44;
    public static final int CHAT_LOCATION = 45;
    public static final int CHAT_RIDE_REQUEST = 46;
    public static final int CHAT_RIDE_OFFER = 47;
    public static final int CHAT_RIDE_STARTED = 48;

    public String id;

    public ArrayList<String> owner;

    public String content;

    public int chatType;

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public String getOwnerId() {
        return owner.get(Utils.ID);
    }

    public String getOwnerRef() {
        return owner.get(Utils.PHOTO_REF);
    }

    public Date created_at;

    public ArrayList<String> likedBy;

    public Chat() {
    }

    public ArrayList<String> getLikedBy() {
        return likedBy;
    }

    public void addLike(String uid) {
        this.likedBy.add(uid);
    }

    public void unLike(String uid) {
        this.likedBy.remove(uid);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOwnerName() {
        return owner.get(Utils.NAME);
    }

    public Date getCreated_at() {
        return (created_at == null ? new Date() : created_at);
    }
}
