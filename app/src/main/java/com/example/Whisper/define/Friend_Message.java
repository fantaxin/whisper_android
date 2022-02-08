package com.example.Whisper.define;

/*
* Friend_Message 用于储存好友的聊天记录
* */

public class Friend_Message {
    public boolean success;

    public long sender_id ;
    public long receiver_id ;
    public String send_time ;
    public int message_type ;
    public String message ;
    public Friend_Message(boolean success, long sender_id, long receiver_id, String send_time, String message){
        this.success = success;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.send_time = send_time;
        this.message = message;
    }
    public Friend_Message(boolean success, long sender_id, long receiver_id, String send_time, int message_type, String message){
        this.success = success;

        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.send_time = send_time;
        this.message_type = message_type;
        this.message = message;
    }
    public Friend_Message(boolean success){
        this.success=success;
    }
}
