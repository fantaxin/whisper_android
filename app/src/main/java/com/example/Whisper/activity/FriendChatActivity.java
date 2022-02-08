package com.example.Whisper.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.Whisper.define.Friend;
import com.example.Whisper.adapter.FriendAdapter;
import com.example.Whisper.define.Msg;
import com.example.Whisper.R;

import java.util.ArrayList;
import java.util.List;
/*此活动用来显示好友列表*/
public class FriendChatActivity extends BaseActivity {

  //  private String[] data={"image1","image2","image3","image4","image5","image6","image7","image8","image9","image10","image11","image12","image13"};
    //转为全局变量private List<Friend> FriendList=new ArrayList<>();
    FriendAdapter adapter;
    ListView listView;
    ImageView imageView;

    private ImageView main_interface;
    private ImageView message_interface;
    private ImageView web;
    private ImageView exit;
    private DrawerLayout drawerLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.update:
                Toast.makeText(this,"刷新好友列表",Toast.LENGTH_SHORT).show();
                /*向服务器发送申请列表*/
                Msg msg=new Msg(Msg.TYPE_GET_ONLINELIST,user_id,mainserverIp,12000,"get user online list");//构造自定义协议内容
                Log.d("msg","发送消息构造完成，内容为"+msg.getContent());
                tcp_sender.putMsg(msg);//将要发送内容设置好
                tcp_sender_tread.interrupt();//网络子线程开始运行
                break;
            case R.id.add:
                Toast.makeText(this,"添加",Toast.LENGTH_SHORT).show();
                break;
            case R.id.scan:
                Toast.makeText(this,"扫描",Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;
    }

    /*活动创建时先加载一次在线列表*/
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendchat);
        imageView=(ImageView)findViewById(R.id.back);
        setSupportActionBar(findViewById(R.id.toolbar));//如果不这样，菜单则不会显示，但注意此时如果toolbar中没有现在的新布局，则会显示androidmanifrst中的应用名/活动中的label
/*        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null) actionBar.hide();*/
        View decorView = getWindow().getDecorView();
        //设置导航栏颜色为透明
        getWindow().setNavigationBarColor(Color.parseColor("#EDEDED"));
        //设置通知栏颜色为透明
        getWindow().setStatusBarColor(Color.parseColor("#EDEDED"));
        //这只状态栏字体颜色为深色
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        main_interface = (ImageView) findViewById(R.id.main_interface);
        message_interface = (ImageView) findViewById(R.id.message_interface);
        web = (ImageView) findViewById(R.id.web);
        exit = (ImageView) findViewById(R.id.exit);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        tcp_sender.setHandler(handler);//根据不同活动中设置，可以使网络线程发送到不同活动中。
        /*向服务器发送申请，获取在线列表*/
        /*接受:[TYPE_GET_ONLINE]\r\n[sender_id]\r\n[get_online]
          发送:[TYPE_GET_ONLINE]\r\n[sender_id]\r\n[(id)|(name)@@(id2)|(name2)]*/
        Msg msg=new Msg(Msg.TYPE_GET_ONLINELIST,user_id,"get online list");//构造自定义协议内容
        Log.d("msg","发送消息构造完成，内容为"+msg.getContent());
        //tcp_sender_tread = new Thread(tcp_sender);
        tcp_sender.putMsg(msg);//将要发送内容设置好
        tcp_sender_tread.interrupt();//网络子线程开始运行


        adapter=new FriendAdapter(FriendChatActivity.this,R.layout.friend_item ,FriendList);//实例化适配器
        listView=(ListView)findViewById(R.id.list_view);//获取listview实例
        listView.setAdapter(adapter);//给listview设置适配器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Friend Friend=FriendList.get(position);//获取对应项内容
                Toast.makeText(FriendChatActivity.this,Friend.getName(),Toast.LENGTH_SHORT).show();
                chat_aim=Friend;

                BaseActivity.dataBase.openDataBase();
                BaseActivity.dataBase.add_friend(Friend.getId(),Friend.getName(),Friend.getImageId());

                Intent intent=new Intent(FriendChatActivity.this,MsgActivity.class);//点击时跳转到聊天页面
                startActivity(intent);
            }
        });//每一项被点击时执行的操作

        main_interface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        message_interface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Friend Friend = new Friend(true);//获取对应项内容
                chat_aim = Friend;
                Intent intent = new Intent(FriendChatActivity.this, MsgActivity.class);//点击时跳转到聊天页面
                startActivity(intent);
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();//退出程序
            }
        });

    }
    /*此活动需要处理的网络消息为好友列表，当网络线程获取到数据时，会调用此方法*/
    @Override
    public void processMessage(Message msg) {
        Log.d("pro","好友列表活动处理消息");
        String content=msg.getData().getString("content");//网络线程传过来的内容
        Log.d("msgProssess_Chat","msg.what（msgtype） "+msg.what+"\nmsg携带的bundle（msgcontent）内容如下\n"+content);
        switch (msg.what){
//            case Msg.TYPE_ONLINE_LIST :{
//                Log.d("msgProssess_Chat","收到在线列表"+content);
//                Friend temp=new Friend(content,R.drawable.image_1);
//                FriendList.add(temp);
//            }
            case Msg.TYPE_GET_ONLINELIST:{
                Log.d("msgProssess_Chat","收到申请的在线列表"+content);
                initFriends();
                List<Friend> tempfriendlist=new ArrayList<>();
                String[] templist=content.split("@@");
                for(int i=0;i<templist.length;i++)
                {
                    String[]tempfriend=templist[i].split(":");//注意转义字符！
                    Log.d("test", tempfriend[0]+"!!"+tempfriend[1]);
                    Friend temp=new Friend(Integer.parseInt(tempfriend[0]),tempfriend[1],R.drawable.image_6);
                    FriendList.add(temp);
                }
                adapter.notifyDataSetChanged();
                        //notifyItemInserted(msgList.size()-1);//更新适配器，通知适配器消息列表有新的数据插入
                listView.smoothScrollToPosition(FriendList.size()-1);//显示最新的消息，定位到最后一行
            }
        }
    }

    private void initFriends() {
        FriendList.clear();
        Friend image1 = new Friend("图灵机器人", R.drawable.image_1);
        FriendList.add(image1);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}