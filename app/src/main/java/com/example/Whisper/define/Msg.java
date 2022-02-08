package com.example.Whisper.define;
/*该类为自定义协议内容，用于与服务器或其他客户端交换数据，当前主要包含三部分，消息类型，发送者id，消息内容*/
public class Msg extends Object {
    /*客户端点对点通信的消息类型*/
    public static final int TYPE_RECEIVE =0;
    public static final int TYPE_SENT=1;

    /*未知ID下标记通信的发送方*/
    public static final int SERVER = 0;
    public static final int CLIENT = 1;

    /*向服务器发送请求的消息类型*/
    public static final int TYPE_LOGIN = 2;
    public static final int TYPE_REGISTER = 3;
    public static final int TYPE_PRIVATE_CHAT = 4;//私聊
    public static final int TYPE_PUBLIC_GROUP = 5;//公共群聊
    public static final int TYPE_GET_ONLINE = 6; public static final int TYPE_GET_ONLINELIST=TYPE_GET_ONLINE;
    public static final int TYPE_EXIT = 9; public static final int TYPE_QUIT=TYPE_EXIT;

    public static final int TYPE_RECEIVE_BROADCAST=60;

    public static final int TYPE_TULING_OK=100;

    public static final long PUBLIC_GROUP_ID = 1000;
    /*服务器连接问题*/
    public static final int ER_TIME_OUT = 111;
    public static final int ER_NO_CONN = 110;
    public static final int SERVER_READY = 111111;


    /*public static final int TYPE_KEEP_CONNECTION = 7;

    public static final int TYPE_ADD_FRIEND = 3;
    public static final int TYPE_ADD_GROUP = 4;
    public static final int TYPE_CREATE_GROUP = 5;
    public static final int TYPE_MATCH = 6;*/

    /*向Msg_Net线程发送阻塞消息*/
    public static final int NET_BLOCK = 94;
    public static final int NET_BLOCK_SUCCESS = 95;

    /*登录注册部分服务器返回消息*/
    public static final int REGISTER_SUCCESS = 1000;//注册成功
    public static final int REGISTER_DEFAULT = 1001;//注册失败(账号已存在)
    public static final int LOGIN_SUCCESS = 1100;
    public static final int PASSWORD_ERROR = 1011;
    public static final int UNREGISTER = 1101;
    public static final int OTHER_ERROR = 1111;

    private int type;//消息类型
    private long sender_id;//发送者id，0为服务端，或者默认id
    String ip = "null";//目的ip
    int port = 0;//目的端口
    private String content;//消息内容
    private String serder_name;

    /*构造函数之构造消息*/
    public Msg(int type,long sender_id,String ip,int port,String content){
        this.content=content;
        this.type=type;
        this.ip=ip;
        this.port=port;
        this.sender_id=sender_id;
    }
    /*构造函数之解析消息，将网络线程返回的消息（String类型）重新构造成Msg类型*/
    public Msg(String temp){
        String[] list=temp.split("\r\n");
        type=Integer.parseInt(list[0]);
        sender_id=Integer.parseInt(list[1]);
        content=list[2];
    }
    public Msg(int type, int sender_id, String content) {
        this.type = type;
        this.sender_id = sender_id;
        this.content = content;
    }
    public Msg(int type, long sender_id, String content) {
        this.type = type;
        this.sender_id = sender_id;
        this.content = content;
    }
    public Msg(int type, long sender_id, String sender_name, String content) {
        this.type = type;
        this.sender_id = sender_id;
        this.serder_name = sender_name;
        this.content = content;
    }
    public String getContent(){
        return content;
    }
    public long getsender_id(){
        return sender_id;
    }
    public int getType(){
        return type;
    }
    public int getPort(){
        return port;
    }
    public String getIp(){
        return ip;
    }
    public String getSender_name(){
        return serder_name;
    }
    public String[] parseContent()
    {
        String[] list ;
        switch(type)
        {
            case TYPE_REGISTER:
            case TYPE_LOGIN: {
                list = content.split("@@");
                break;
            }
            default: {
                list = new String[1];
                list[0] = content;
                break;
            }
        }
        return list;
    }
}
