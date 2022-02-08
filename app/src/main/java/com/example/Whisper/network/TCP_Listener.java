package com.example.Whisper.network;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.Whisper.activity.BaseActivity;
import com.example.Whisper.define.Msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.Thread.sleep;

/*当用户处于聊天界面时，使用本线程接收消息，如果收到的是该聊天对象的消息，存入数据库并显示，否则只存入数据库*/
public class TCP_Listener implements Runnable{
    static Handler handler;
    public boolean onWork = true;    //线程工作标识
    public static void setHandler(Handler handler1) {
        handler = handler1;
    }

    Socket socket;
    ServerSocket serverSocket = BaseActivity.serverSocket;

    private void TCPSend(Socket socket, Msg reply) throws IOException {
        String str_reply = reply.getType() + "\r\n" + reply.getsender_id() + "\r\n" + reply.getContent();
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF(str_reply);
        System.out.println("reply:"+reply.getType()+","+reply.getsender_id()+","+reply.getContent());
    }
    /*
     * 1.2私聊：
     * 接收:[TYPE_PRIVATE_CHAT]\r\n[sender_id]\r\n[(receive_id)@@(message_content)]
     * 通过receive在online中查找ip和port,然后在socket_list中查找相应的socket,转发
     * 转发:[TYPE_PRIVATE_CHAT]\r\n[sender_id]\r\n[message_content]
     * 1.3公共群聊：
     * 接收:[TYPE_PUBLIC_GROUP]\r\n[sender_id]\r\n[(group_id)@@(message_content)]
     * 依次在socket_list中转发
     * 转发:[TYPE_PUBLIC_GROUP]\r\n[sender_id]\r\n[(group_id)@@(message_content)]
     */
    @Override
    public void run() {
        while (true) {
            Log.d("TCP_L", "tcpl正在运行中");
            try {
                //sleep(1000);
                socket = serverSocket.accept();
                DataInputStream in = new DataInputStream(socket.getInputStream());
                String temp = in.readUTF();
                if (!temp.isEmpty())//防止出现空包导致卡死？
                {
                    Msg msg = new Msg(temp);
                    Message message = new Message();
                    message.what = Integer.valueOf(msg.getType());//what字段带有消息类型

                    if(Integer.valueOf(message.what).equals(Msg.NET_BLOCK)) {
                        Log.d("TCP_L", "已销毁");
                        Msg reply = new Msg(Msg.NET_BLOCK_SUCCESS,0,"block success");
                        TCPSend(socket,reply);
                        socket.close();
                        break;
                    }

                    message.arg1 = (int) msg.getsender_id();//arg1字段带有发送者id
                    Bundle data = new Bundle();//携带较多数据时的方法，带有消息正文
                    data.putString("content", msg.getContent());
                    message.setData(data);
                    handler.sendMessage(message);
                }
                socket.close();
                Log.d("TCP_L", "tcpl本次通信结束");
                //} catch (IOException e) {
                //  e.printStackTrace();
                // }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
