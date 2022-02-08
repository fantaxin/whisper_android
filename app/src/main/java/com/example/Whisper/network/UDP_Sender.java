package com.example.Whisper.network;

import android.os.Handler;
import android.util.Log;

import com.example.Whisper.define.Msg;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

public class UDP_Sender implements Runnable {
    public boolean onWork = true;    //线程工作标识

    static Handler handler;
    Queue<Msg> queue = new LinkedList<Msg>();//发送队列
    public void putMsg(Msg msg){queue.offer(msg);}

    public static void setHandler(Handler handler1) {
        handler = handler1;
    }



    /*发送UDP数据报，线程运行时也可调用NetThread_UDP中的其他方法*/
    //udp传输：
    /**步骤：---
     * 1、建立udp socket 接收和发送点
     * 2、提供数据，打包
     * 3、使用send发送
     * 4、关闭资源
     * */
    public void sendUdpData(Msg msg) {
       try{
        // 创建发送端Socket, 绑定本机IP地址, 绑定任意一个未使用的端口号
        DatagramSocket socket = new DatagramSocket();
        // 创建发送端Packet, 指定数据, 长度, 地址, 端口号
        String temp = msg.getType() + "\r\n" + msg.getsender_id() + "\r\n" + msg.getContent();
        DatagramPacket packet = new DatagramPacket(temp.getBytes("UTF-8"), temp.getBytes().length, InetAddress.getByName(msg.getIp()),msg.getPort());
        // 使用Socket发送Packet
        socket.send(packet);
        // 关闭Socket
        socket.close();
           Log.d("udp","输出流\n"+"目的IP"+msg.getIp()+" 端口"+String.valueOf(msg.getPort())+"\n发送id "+String.valueOf(msg.getsender_id())+"\n消息类型 "+String.valueOf(msg.getType())+"\ncontent "+msg.getContent());
       } catch (SocketException | UnsupportedEncodingException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }
    }
    /*UDP采用消息队列发送消息*/
    @Override
    public void run() {
        while(onWork) {
            while (!queue.isEmpty()) {
                sendUdpData(queue.poll());
            }
            try {
                Thread.sleep(100000);//为空时休眠，等待唤醒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*线程运行时调用的方法，一直监听一个UDP端口，当前似乎无法收到服务端发来的数据*/
    //udp传输： 接收
    /**步骤：---
     * 1、建立udp socket，设置接收端口
     * 2、预先创建数据存放的位置，封装
     * 3、使用receive阻塞式接收
     * 4、关闭资源
     * */

}
