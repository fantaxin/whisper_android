package com.example.Whisper.network;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.Whisper.define.Msg;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDP_Listener implements Runnable {
    public boolean onWork = true;    //线程工作标识
    static Msg msg;
    int port = 11000;
    static Handler handler;
    public static void setHandler(Handler handler1) {
        handler = handler1;
    }
    /*UDP接收消息，暂时没有在网络线程中采用队列暂存*/
    @Override
    public void run() {
        try {
            // 创建接收端Socket, 绑定本机IP地址, 绑定指定端口
            DatagramSocket socket = new DatagramSocket(port);

            // 创建接收端Packet, 用来接收数据
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            while (onWork) {
                Log.d("udp", "监听UDP"+socket.getLocalAddress()+socket.getPort());
                // 用Socket接收Packet, 未收到数据时会阻塞
                socket.receive(packet);
                // 从Packet中获取数据
                byte[] temp = packet.getData();
                int len = packet.getLength();
                String result = new String(temp, 0, len, "UTF-8");
                Msg reply = new Msg(result);//解析消息
                Log.d("udp","返回流\n来自"+socket.getRemoteSocketAddress()+"\n发送者id "+String.valueOf(reply.getsender_id())+"\n消息类型 "+String.valueOf(reply.getType())+"\ncontent "+reply.getContent());

                /*将信息通过handle从当前子线程发送给主线程*/
                Message message=new Message();
                message.what=Integer.valueOf(reply.getType());//what字段带有消息类型
                message.arg1= (int) reply.getsender_id();//arg1字段带有发送者id
                Bundle data=new Bundle();//携带较多数据时的方法，带有消息正文
                data.putString("content",reply.getContent());
                message.setData(data);
                handler.sendMessage(message);
            }
            // 关闭Socket
            socket.close();
        } catch (SocketException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
