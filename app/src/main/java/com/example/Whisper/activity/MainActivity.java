package com.example.Whisper.activity;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.Whisper.adapter.messageAdapter;
import com.example.Whisper.define.Friend;
import com.example.Whisper.define.Msg;
import com.example.Whisper.R;
import com.example.Whisper.define.messageItem;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;


/*此活动为主界面，集成了各种不同的功能模块，也包含了公告栏，此活动登录后一直存在，不被销毁*/
public class MainActivity extends BaseActivity {
    //private Button friendchat;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ImageView main_interface;
    private ImageView message_interface;
    private ImageView web;
    private ImageView exit;
    private Toolbar toolbar;

    private ListView message_list;
    private ImageView setting;
    private messageAdapter adapter;//主界面消息的适配器
    private List<messageItem> MsgList = new ArrayList<>();

    public static class MyCallback implements ComponentCallbacks {
        @Override
        public void onConfigurationChanged(Configuration arg) {
        }
        @Override
        public void onLowMemory() {
            Log.d("销毁1", "main");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyCallback callBacks = new MyCallback();
        this.registerComponentCallbacks(callBacks);
        this.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                //sActivity = activity; //可以获取到栈顶对象
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d("销毁", "main");
            }
        });
        setContentView(R.layout.activity_main);
        /*集中创建网络线程*/
        tcp_sender.setHandler(handler);
        net_listener_tread = new Thread(net_listener);
        Msg_need_lock = false;
        net_listener_tread.start();

        udp_sender.setHandler(handler);
        udp_listener.setHandler(handler);
        udp_sender_tread = new Thread(udp_sender);//发送udp消息
        udp_listener_tread = new Thread(udp_listener);//接收udp消息
        udp_sender_tread.start();
        udp_listener_tread.start();

        //friendchat = (Button) findViewById(R.id.friendchat);

        main_interface = (ImageView) findViewById(R.id.main_interface);
        message_interface = (ImageView) findViewById(R.id.message_interface);
        web = (ImageView) findViewById(R.id.web);
        exit = (ImageView) findViewById(R.id.exit);

        message_list = (ListView) findViewById(R.id.message_list);
        message_list.setVisibility(View.INVISIBLE);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(findViewById(R.id.toolbar));
        setting = (ImageView) findViewById(R.id.setting);


        View decorView = getWindow().getDecorView();
        //设置导航栏颜色为透明
        getWindow().setNavigationBarColor(Color.parseColor("#EDEDED"));
        //设置通知栏颜色为透明
        getWindow().setStatusBarColor(Color.parseColor("#EDEDED"));
        //这只状态栏字体颜色为深色
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.logo_mini);
        }
        navigationView.setCheckedItem(R.id.nav_call);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //可以在这里设置逻辑，这里只是用nav_call做一个示范
                drawerLayout.closeDrawers();
                return true;
            }
        });


        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "设置", Toast.LENGTH_SHORT).show();
            }
        });

        main_interface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ssss", "1111111");
            }
        });
        message_interface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FriendChatActivity.class);//打开聊天模块
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Friend Friend = new Friend(true);//获取对应项内容
                chat_aim = Friend;
                Intent intent = new Intent(MainActivity.this, MsgActivity.class);//点击时跳转到聊天页面
                startActivity(intent);
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //服务器从在线列表中将其删除
                Msg msg = new Msg(Msg.TYPE_EXIT, user_id, "goodbye");
                Log.d("msg", "发送消息构造完成，内容为" + msg.getContent());
                tcp_sender.putMsg(msg);//将要发送内容设置好
                tcp_sender_tread.interrupt();//网络子线程开始运行
                finish();//退出程序
            }
        });

        adapter = new messageAdapter(MainActivity.this, R.layout.message_item, MsgList);//实例化适配器
        message_list.setAdapter(adapter);//给listview设置适配器
        message_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                messageItem messageItem = MsgList.get(position);//获取对应项内容
                //临时测试设置
                messageItem.setFriend(new Friend(111, "发送者", R.drawable.image_6, "127.0.0.1", 11000));
                Toast.makeText(MainActivity.this, messageItem.getName(), Toast.LENGTH_SHORT).show();

                chat_aim = messageItem.getFriend();

                Intent intent = new Intent(MainActivity.this, MsgActivity.class);//点击时跳转到聊天页面
                startActivity(intent);
            }
        });
        messageItem temp = new messageItem("类型" + String.valueOf(5) + "id" + String.valueOf(22), R.drawable.image_5, "test");
        for (int i = 0; i < 10; i++) {
            MsgList.add(temp);
            MsgList.add(temp);
            MsgList.add(temp);
            MsgList.add(temp);
        }
    }

    /*处理网络线程返回的信息*/
    @Override
    public void processMessage(Message msg) {
        Log.d("pro", "主活动处理消息");

        String content = msg.getData().getString("content");
        Log.d("msgProssess_Main", "msg.what（msgtype） " + msg.what + "\nmsg携带的bundle（msgcontent）内容如下\n" + content);
        switch (msg.what) {
            case Msg.TYPE_RECEIVE_BROADCAST: {
                //broad.setText("[公告栏]\n" + content);
                break;
            }
            case Msg.TYPE_SENT:
                //注意图灵传递消息是通过msg.obj,而自己写的则是bundle带的数据！
                Log.d("mark显示测试", content);
                messageItem temp = new messageItem("类型" + String.valueOf(msg.what) + "id" + String.valueOf(msg.arg1), R.drawable.image_1, content);

                //查找发送者ip

                temp.setFriend(new Friend(msg.arg1, "发送者", R.drawable.image_6, "", 11000));
                MsgList.add(temp);
                adapter.notifyDataSetChanged();
                //notifyItemInserted(msgList.size()-1);//更新适配器，通知适配器消息列表有新的数据插入
                message_list.smoothScrollToPosition(MsgList.size() - 1);//显示最新的消息，定位到最后一行
                //message_list
                //showData(content);

                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        tcp_sender.setHandler(handler);
        udp_sender.setHandler(handler);
        udp_listener.setHandler(handler);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("销毁", "main");
        /*向服务器发送退出登录信息*/
        Msg msg = new Msg(Msg.TYPE_QUIT, user_id, mainserverIp, 12000, "quit");
        Log.d("msg", "消息构造完成");
        udp_sender.putMsg(msg);
        udp_sender_tread.interrupt();

    }
}
