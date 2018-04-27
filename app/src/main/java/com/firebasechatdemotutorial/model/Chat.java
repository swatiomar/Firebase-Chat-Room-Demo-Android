package com.firebasechatdemotutorial.model;

/**
 * Created by mobua01 on 25/4/18.
 */

public class Chat {
    public String sender;
    public String receiver;
    public String senderUid;
    public String receiverUid;
    public String message;
    public String latitude;
    public String longitude;
    public String type;
    public long timestamp;

    public Chat() {

    }

    public Chat(String sender, String receiver, String senderUid, String receiverUid, String message, long timestamp, String lat, String log) {
        this.sender = sender;
        this.receiver = receiver;
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.message = message;
        this.timestamp = timestamp;
        this.latitude = lat;
        this.longitude = log;
//        this.type = type;
    }

}
