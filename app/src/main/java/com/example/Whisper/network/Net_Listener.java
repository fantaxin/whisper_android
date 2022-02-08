package com.example.Whisper.network;

import android.util.Log;

import com.example.Whisper.activity.BaseActivity;
import com.example.Whisper.define.Msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.Whisper.activity.BaseActivity.dataBase;
import static com.example.Whisper.activity.BaseActivity.user_id;
import static com.example.Whisper.activity.BaseActivity.Msg_lock;
import static com.example.Whisper.activity.BaseActivity.Msg_need_lock;
import static java.lang.Thread.sleep;

/*当用户不在聊天界面时，此线程用于接收消息，并存入本地数据库中*/
public class Net_Listener implements Runnable {

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
     * 转发:[TYPE_PUBLIC_GROUP]\r\n[sender_id]\r\n[(message_content)]
     */
    @Override
    public void run() {
        while (true) {
            try {
                if(Msg_need_lock)
                    synchronized(Msg_lock) {
                    Msg_lock.wait();
                }
                //sleep(100);
                Log.d("Net", "net正在运行中");
                socket = serverSocket.accept();
                DataInputStream in = new DataInputStream(socket.getInputStream());
                String temp = in.readUTF();
                if (!temp.isEmpty())//防止出现空包导致卡死？
                {
                    Msg msg = new Msg(temp);
                    String[] list = msg.parseContent();
                    Msg reply;
                    switch (msg.getType()) {
                        case Msg.NET_BLOCK:{
                            Log.d("Net", "已阻塞");
                            reply = new Msg(Msg.NET_BLOCK_SUCCESS,0,"block success");
                            TCPSend(socket,reply);
                            synchronized(Msg_lock) {
                                Msg_lock.wait();
                            }
                            break;
                        }
                        case Msg.TYPE_PRIVATE_CHAT: {
                            dataBase.add_chatFile(msg.getsender_id(), user_id, msg.getContent());
                            break;
                        }
                        case Msg.TYPE_PUBLIC_GROUP:{
                            dataBase.add_public_chatFile(msg.getsender_id(), user_id, msg.getContent());
                        }
                        default:
                            break;
                    }
                }
                socket.close();
                Log.d("Net", "net本次通信结束");
            } catch (SocketTimeoutException s) {
                System.out.println("Receive socket timed out!");
            } catch (EOFException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //private void execute(Msg msg) throws IOException{

        //}
    }
}
