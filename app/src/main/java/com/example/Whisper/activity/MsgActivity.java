package com.example.Whisper.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.Whisper.define.Friend_Message;
import com.example.Whisper.define.Msg;
import com.example.Whisper.adapter.MsgAdapter;
import com.example.Whisper.R;
import com.example.Whisper.network.TCP_Listener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/*此活动为聊天界面*/
public class MsgActivity extends BaseActivity {
    /*接入图灵机器人*/
    private static final String WEB_SITE = "http://openapi.tuling123.com/openapi/api/v2";//接口地址
    private static final String KEY = "2bf269c26b324fc8bcf8d2b332313182";//apikey
    private SwipeRefreshLayout swipeRefreshLayout;
    /*    class MHandler extends Handler{
            @Override
            public void dispatchMessage(Message msg){
                super.dispatchMessage(msg);
                Log.d("mark显示接收消息内容1",(String)msg.obj);
                switch (msg.what){
                    case Msg.TYPE_TULING_OK:
                        if(msg.obj!=null){
                            String vlResult=(String)msg.obj;
                            parseData(vlResult);
                        }
                        break;
                }
            }
        }
        private MHandler mHandler;*/
    private void parseData(String vlResult) {
        try {
            JSONObject obj = new JSONObject(vlResult);
            JSONObject intent = obj.getJSONObject("intent");
            int code = intent.getInt("code");
            JSONArray results = obj.getJSONArray("results");
            Log.d("mark显示接收消息内容21", String.valueOf(results.length()));

            for (int i = 0; i < results.length(); i++) {
                String content = "";
                JSONObject result = results.getJSONObject(i);
                JSONObject values = result.getJSONObject("values");
                if (values.has("text"))
                    content = values.getString("text");
                if (values.has("url")) {
                    content = values.getString("url");
                    inputText.setText(content);
                }
            /*    if(results.length()==1)
                    content=values.getString("text");
                else */
                Log.d("mark显示接收消息内容2", content);
                switch (code) {
                    case 4003:
                        showData("主人，我今天累了，我要休息了，明天再来找我耍吧");
                        continue;
                    default:
                        showData(content);
                        continue;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showData("主人，你的网络不太好哦");
        }
    }

    private void showData(String content) {
        /*新增消息时的操作*/
        Msg msg = new Msg(Msg.TYPE_RECEIVE, 0,mainserverIp,12000, content);
        msgList.add(msg);//添加消息到消息列表
        Log.d("mark显示接收消息内容3", content);
        adapter.notifyItemInserted(msgList.size() - 1);//更新适配器，通知适配器消息列表有新的数据插入
        msgRecyclerView.scrollToPosition(msgList.size() - 1);//显示最新的消息，定位到最后一行
    }

    //private List<Msg> msgList = new ArrayList<>();//消息列表
    private LinkedList<Msg> msgList = new LinkedList<>();//消息列表
    private List<Msg> linkedList = new LinkedList<>();

    private List<Friend_Message> fmList = new ArrayList<Friend_Message>();
    private int msg_num = 0;
    private EditText inputText;//输入消息内容
    private Button send;//发送按钮
    private RecyclerView msgRecyclerView;//此控件用于显示消息
    private MsgAdapter adapter;//RecyclerView的适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Msg_need_lock = true;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg);//注意指向的布局

        tcp_sender.setHandler(handler);
        /*本地TCP通信，阻塞Net_Listener线程*/
        Msg block_msg=new Msg(Msg.NET_BLOCK,Msg.CLIENT,serverSocket.getInetAddress().getHostAddress(),localserverSocketPort,"block");
        tcp_sender.putMsg(block_msg);
        tcp_sender_tread.interrupt();

        tcp_listener.setHandler(handler);
        tcp_listener_tread = new Thread(tcp_listener);
        tcp_listener_tread.start();

        udp_sender.setHandler(handler);
        udp_listener.setHandler(handler);

        if (chat_aim.getName().equals("图灵机器人"))
            initMsgs();//初始化消息列表，当前未使用网络线程，故使用本地数据
        else {
            tempinitMsgs();
        }


        inputText = (EditText) findViewById(R.id.input_text);
        send = (Button) findViewById(R.id.send);
        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);//布局管理器
        msgRecyclerView.setLayoutManager(layoutManager);//给RecyclerView设置布局管理器
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);//给RecyclerView设置适配器
        //Log.d("mark", msgList.get(1).getContent());//从List中获取内容的方法

        setSupportActionBar(findViewById(R.id.toolbar));
        TextView name = findViewById(R.id.name);
        name.setText(chat_aim.getName());


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = inputText.getText().toString();
                if (!"".equals(content)) {
                    /*新增消息时的操作*/
                    Msg msg = new Msg(Msg.TYPE_SENT, user_id, content);
                    msgList.add(msg);//添加消息到消息列表
                    Log.d("mark", content);
                    adapter.notifyItemInserted(msgList.size() - 1);//更新适配器，通知适配器消息列表有新的数据插入
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);//显示最新的消息，定位到最后一行
                    inputText.setText("");

                    dataBase.add_chatFile(user_id,chat_aim.getId(),content);

                    /*根据消息类型判定发送对象*/
                    if (chat_aim.getName().equals("图灵机器人"))//向图灵服务器发送数据
                        sendData(content);
                     /* 接收:[TYPE_PUBLIC_GROUP]\r\n[sender_id]\r\n[(group_id)@@(message_content)]
                      * 转发:[TYPE_PUBLIC_GROUP]\r\n[sender_id]\r\n[(message_content)]*/
                    else if(Integer.valueOf((int) chat_aim.getId()).equals((int)Msg.PUBLIC_GROUP_ID)){
                        Msg send_msg = new Msg(Msg.TYPE_PUBLIC_GROUP, user_id,
                                user_name+"::"+content);
                        tcp_sender.putMsg(send_msg);
                        tcp_sender_tread.interrupt();
                    }
                    else{//私聊
                        //[TYPE_PRIVATE_CHAT]\r\n[sender_id]\r\n[(receive_id)@@(message_content)]
                        Msg send_msg = new Msg(Msg.TYPE_PRIVATE_CHAT,user_id,
                                String.valueOf(chat_aim.getId())+"@@"+chat_aim.getName()+"::"+content);
                        tcp_sender.putMsg(send_msg);
                        tcp_sender_tread.interrupt();
                    }
                }
            }
        });
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.btn_blue_normal);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        msgRecyclerView.scrollToPosition(msgList.size() - 1);//显示最新的消息，定位到最后一行
    }
    private void refresh(){
        //本地刷新测试
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(2000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initMsgs();
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }
    private void sendData(String content) {//content已不为空
        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
        String jsonStr =
                "{ \"reqType\":0," +
                "\"perception\": " +
                "{\"inputText\": " +
                "{\"text\": " + "\"" + content + "\" }," +
                "\"inputImage\": " + "{\"url\": \"imageUrl\" }," +
                "\"selfInfo\": {\"location\": {\"city\": \"北京\",\"province\": \"北京\",\"street\": \"信息路\" } } }," +
                "\"userInfo\": " + "{\"apiKey\": \"2bf269c26b324fc8bcf8d2b332313182\"," + "\"userId\": \"" + user_id + "\" } " + "}";
        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request request = new Request.Builder()
                .url(WEB_SITE)
                .post(body)
                .build();
        // Request request=new Request.Builder().url(WEB_SITE+"?key="+KEY+"&info="+content).build();
        Call call = okHttpClient.newCall(request);
        //开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                Message msg = new Message();
                msg.what = Msg.TYPE_TULING_OK;
                msg.obj = res;
                Log.d("mark显示接收消息内容0", res);
                handler.sendMessage(msg);//注意此处是个独立的Handler，需要在MsgActiviy中再次写
            }
        });
    }

    /*收到消息在这里处理*/
    @Override
    public void processMessage(Message msg) {
        switch (msg.what) {
            case Msg.TYPE_TULING_OK:
                if (msg.obj != null) {
                    String vlResult = (String) msg.obj;
                    parseData(vlResult);
                }
                break;
            case Msg.TYPE_SENT: {
                //注意图灵传递消息是通过msg.obj,而自己写的则是bundle带的数据！
                String content = msg.getData().getString("content");
                Log.d("mark显示测试", content);
                showData(content);
                break;
            }
            case Msg.TYPE_PRIVATE_CHAT: {
                //[TYPE_PRIVATE_CHAT]\r\n[sender_id]\r\n[message_content]
                /*新增消息时的操作*/
                String content=msg.getData().getString("content");
                String list[] = content.split("::");
                Msg m = new Msg(Msg.TYPE_RECEIVE, user_id, list[0],list[1]);
                if(Integer.valueOf(msg.arg1).equals((int)chat_aim.getId())) {


                    msgList.add(m);//添加消息到消息列表
                    adapter.notifyItemInserted(msgList.size() - 1);//更新适配器，通知适配器消息列表有新的数据插入
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);//显示最新的消息，定位到最后一行


                }
                dataBase.add_chatFile(msg.arg1, user_id, content);
            }
            case Msg.TYPE_PUBLIC_GROUP:{
                //转发:[TYPE_PUBLIC_GROUP]\r\n[sender_id]\r\n[(message_content)]
                //msg.arg1是sender_id
                String content=msg.getData().getString("content");
                String list[] = content.split("::");

                /****/
                Msg m = new Msg(Msg.TYPE_RECEIVE, user_id, list[0],list[1]);
                //msgList.addFirst(m);
                if(Integer.valueOf((int)Msg.PUBLIC_GROUP_ID).equals((int)chat_aim.getId())) {

                    msgList.add(m);//添加消息到消息列表
                    adapter.notifyItemInserted(msgList.size() - 1);//更新适配器，通知适配器消息列表有新的数据插入
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);//显示最新的消息，定位到最后一行


                }
                dataBase.add_public_chatFile(msg.arg1, user_id, content);
            }

        }
    }

    /*向上划获取更老的消息*/
    private void initMsgs() {//采用LinkedList的头插，获取更老的消息
        fmList = dataBase.search_chatFile(chat_aim.getId(),msg_num);
        msg_num += fmList.size();
        for(int i = 0;i<fmList.size();i++)
        {
            Msg msg;
            String list[] = fmList.get(i).message.split("::");
            if(Integer.valueOf(list.length).equals(2)) {
                if (Integer.valueOf((int) fmList.get(i).sender_id).equals((int) user_id))
                    msg = new Msg(Msg.TYPE_SENT, user_id, fmList.get(i).message);
                else
                    msg = new Msg(Msg.TYPE_RECEIVE, user_id, list[0], list[1]);
            }
            else
            {
                if (Integer.valueOf((int) fmList.get(i).sender_id).equals((int) user_id))
                    msg = new Msg(Msg.TYPE_SENT, user_id, fmList.get(i).message);
                else
                    msg = new Msg(Msg.TYPE_RECEIVE, user_id, chat_aim.getName(),fmList.get(i).message);
            }
            msgList.addFirst(msg);
        }
    }

    private void tempinitMsgs() {
        fmList = dataBase.search_chatFile(chat_aim.getId(),msg_num);
        msg_num += fmList.size();
        for(int i = 0;i<fmList.size();i++)
        {
            Msg msg;
            String list[] = fmList.get(i).message.split("::");
            if(Integer.valueOf(list.length).equals(2)) {
                if (Integer.valueOf((int) fmList.get(i).sender_id).equals((int) user_id))
                    msg = new Msg(Msg.TYPE_SENT, user_id, fmList.get(i).message);
                else
                    msg = new Msg(Msg.TYPE_RECEIVE, user_id, list[0], list[1]);
            }
            else
            {
                if (Integer.valueOf((int) fmList.get(i).sender_id).equals((int) user_id))
                    msg = new Msg(Msg.TYPE_SENT, user_id, fmList.get(i).message);
                else
                    msg = new Msg(Msg.TYPE_RECEIVE, user_id, chat_aim.getName(),fmList.get(i).message);
            }
            msgList.addFirst(msg);
        }
    }

    @Override
    protected void onDestroy() {

        Msg block_msg=new Msg(Msg.NET_BLOCK,Msg.CLIENT,"127.0.0.1",localserverSocketPort,"block");
        tcp_sender.putMsg(block_msg);
        tcp_sender_tread.interrupt();

        synchronized(Msg_lock) {
            Msg_lock.notify();
        }
        Msg_need_lock = false;

        super.onDestroy();
    }
}