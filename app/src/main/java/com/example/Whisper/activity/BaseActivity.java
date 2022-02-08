package com.example.Whisper.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.example.Whisper.database.DataBase;
import com.example.Whisper.define.ActivityCollector;
import com.example.Whisper.define.Friend;
import com.example.Whisper.define.Msg;
import com.example.Whisper.network.Net_Listener;
import com.example.Whisper.network.TCP_Listener;
import com.example.Whisper.network.TCP_Sender;
import com.example.Whisper.network.UDP_Listener;
import com.example.Whisper.network.UDP_Sender;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/*
此活动继承自AppCompatActivity
所有活动再继承自此抽象活动
在此活动中写一些所有活动都需要用到的功能，如网络线程
*/
public abstract class BaseActivity extends AppCompatActivity {
    public static DataBase dataBase;

    public static String mainserverIp="192.168.137.1";
    public static int mainserverport = 13080;

    public static int localserverSocketPort = 14080;
    public static ServerSocket serverSocket;
    static {
        try {
            serverSocket = new ServerSocket(localserverSocketPort);
            Log.d("ServerSocket", "ip:" + serverSocket.getInetAddress().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static final Object Msg_lock = new Object();//用于阻塞Net_Listener线程
    public static boolean Msg_need_lock = false;

    public static long user_id;//生存期为应用存在全程，记录客户端登录的账号id，在登录时从服务端获取
    public static String user_name;
    protected static Friend chat_aim;//聊天目的方

    protected static int mark=1;//客户端位置信息向服务端发送次数，在LBSactivity（定位活动）中被使用
    protected static  List<Friend> FriendList=new ArrayList<>();//客户端维护的在线列表，从主服务器获取

    /*每个活动创建时，加入到ActivityCollector中*/
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        dataBase = new DataBase(this);
        dataBase.openDataBase();
    }
    /*每个活动销毁时，从ActivityCollector中删除*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
    public abstract void processMessage(Message msg);//在handle中调用此方法，处理handle收到的数据，每个活动都有且需实现的方法
    //静态的变量，多个子类可以共享使用！
    public static TCP_Sender tcp_sender=new TCP_Sender();
    public static Thread tcp_sender_tread;//发送tcp消息
    public static TCP_Listener tcp_listener=new TCP_Listener();
    public static Thread tcp_listener_tread;//接收tcp消息
    public static Net_Listener net_listener = new Net_Listener();//当不在消息界面时，使用该线程接收消息，并将消息存入数据库
    public static Thread net_listener_tread;

    public static UDP_Sender udp_sender=new UDP_Sender();
    public static UDP_Listener udp_listener=new UDP_Listener();
    public static Thread udp_sender_tread;//发送udp消息
    public static Thread udp_listener_tread;//接收udp消息

    public static Queue<Msg> udp_receive_queue = new LinkedList<Msg>();//udp接收队列(只有部分消息会暂存)

    /*handle用于沟通子线程与主线程，在子线程中调用handler.sendMessage(message)后，会调用此处的handleMessage方法*/
    //注意不可为静态的，也就是说每个子类都有一个handler，且要传message给不同活动需要重新设置线程中的handler为目标活动的handler
    protected Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                default:
                    processMessage(msg);//处理消息的方式由子类定义，如何确定哪个活动处理？
            }
        }
    };
//    protected Handler handler2=new Handler(){
//        public void handleMessage(Message msg){
//            switch (msg.what){
//                default:
//                    processMessage(msg);//处理消息的方式由子类定义，如何确定哪个活动处理？
//            }
//        }
//    };
}
