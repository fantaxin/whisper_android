package com.example.Whisper.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Whisper.define.ActivityCollector;
import com.example.Whisper.define.Msg;
import com.example.Whisper.R;
import com.example.Whisper.network.NetWorkUtil;

import java.io.IOException;
import java.net.ServerSocket;

import static com.example.Whisper.define.Msg.ER_NO_CONN;
import static com.example.Whisper.define.Msg.ER_TIME_OUT;
import static com.example.Whisper.define.Msg.LOGIN_SUCCESS;
import static com.example.Whisper.define.Msg.PASSWORD_ERROR;
import static com.example.Whisper.define.Msg.SERVER_READY;
import static com.example.Whisper.define.Msg.UNREGISTER;

/*此活动为登录与注册，保存密码功能采用preference实现*/
public class LoginActivity extends BaseActivity {

    private EditText accountEdit;
    private EditText passwordEdit;
    private Button registerButton;
    private Button loginButton;
    private Button forgetButton;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox remeberPassword;
    private CheckBox autoLogin;
    private TextView status;
    private TextView selfstatus;
    @Override
    protected void onDestroy() {
        super.onDestroy();
          Log.d("销毁","login");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


            requestWindowFeature(Window.FEATURE_NO_TITLE);//初始化窗口为无标题栏的

            //设置状态栏和导航栏颜色为透明
            if (Build.VERSION.SDK_INT >= 21) {
                View decorView = getWindow().getDecorView();
                int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorView.setSystemUiVisibility(option);
                //设置导航栏颜色为透明
                getWindow().setNavigationBarColor(Color.TRANSPARENT);
                //设置通知栏颜色为透明
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
            //隐藏导航栏
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }

        setContentView(R.layout.activity_login);
        accountEdit=(EditText)findViewById(R.id.account);
        passwordEdit=(EditText)findViewById(R.id.password);
        registerButton=(Button)findViewById(R.id.register);
        loginButton=(Button)findViewById(R.id.login);
        pref= getSharedPreferences("login", Context.MODE_PRIVATE);
        remeberPassword=(CheckBox)findViewById(R.id.remember_pass);
        autoLogin=(CheckBox)findViewById(R.id.auto_login);
        status=(TextView) findViewById(R.id.status);
        selfstatus=(TextView) findViewById(R.id.selfstatus);
        boolean isRemember=pref.getBoolean("remember_password",false);
        boolean isAutoLogin=pref.getBoolean("auto_login",false);

        if(ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(LoginActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }

        if(isRemember){
            String account=pref.getString("account","");
            String password=pref.getString("password","");
            accountEdit.setText(account);
            passwordEdit.setText(password);
            remeberPassword.setChecked(true);
            Log.d("local","检测到记住密码已勾选");
        }

        tcp_sender.setHandler(handler);
        tcp_sender_tread=new Thread(tcp_sender);//发送tcp消息
        tcp_sender_tread.start();

        /*查询服务器状态*/
        Msg msg=new Msg(Msg.CLIENT,0,mainserverIp,mainserverport,"isOK?");
        tcp_sender.putMsg(msg);
        tcp_sender_tread.interrupt();

        StringBuilder currentPosition=new StringBuilder();
        currentPosition.append("【您的网络信息】").append("\n");
        currentPosition.append("网络 ").append(NetWorkUtil.isNetworkAvailable(this)).append("\n");
        currentPosition.append("WIFI ").append(NetWorkUtil.isWifi(this)).append("\n");
        currentPosition.append("移动网络 ").append(NetWorkUtil.isMobileNetwork(this)).append("\n");
        currentPosition.append("网络类型 ").append(NetWorkUtil.getNetworkType(this)).append("\n");
        currentPosition.append("提供商 ").append(NetWorkUtil.getProvider(this)).append("\n");
        selfstatus.setText(currentPosition);

        registerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ActivityCollector.activities.get(ActivityCollector.activities.size()-1), RegisterActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String account=accountEdit.getText().toString();
                String password=passwordEdit.getText().toString();
                user_name = account;
                if(account.isEmpty()||password.isEmpty())
                {
                    Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),"账号或密码为空！",Toast.LENGTH_SHORT).show();
                    return;
                }
                /*存入preference的方法*/
                editor=pref.edit();
                if(remeberPassword.isChecked()){
                    editor.putBoolean("remember_password",true);
                    editor.putString("account",account);
                    editor.putString("password",password);
                    Log.d("local","将要存储的账号"+account+"密码"+password);
                }else {
                    editor.clear();
                }
                editor.apply();
                Log.d("local","apply，已存入preference");
                Msg msg=new Msg(Msg.TYPE_LOGIN,0,
                        mainserverIp,mainserverport,
                        account+"@@"+password+"@@"+String.valueOf(localserverSocketPort));//构造自定义协议内容
                Log.d("msg","发送消息构造完成，内容为"+msg.getContent());
                tcp_sender.putMsg(msg);//将要发送内容设置好
                tcp_sender_tread.interrupt();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        tcp_sender.setHandler(handler);
    }

    /*处理网络线程返回的数据*/
    @Override
    public void processMessage(Message msg) {
        Log.d("pro","登录活动处理消息");
        if(msg.what==ER_TIME_OUT)//连接超时异常，验证服务器状态的失败处理
        {
            Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),"无法连接服务器！",Toast.LENGTH_LONG).show();
            status.setText("【服务器配置】\n主服务器IP:"+mainserverIp+"\n状态:不可连接！");
            return;
        }
        if(msg.what==ER_NO_CONN)//连接超时异常，验证服务器状态的失败处理
        {
            Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),"服务器未响应！",Toast.LENGTH_LONG).show();
            status.setText("【服务器配置】\n主服务器IP:"+mainserverIp+"\n状态:不可连接！");
            return;
        }


        String content=msg.getData().getString("content");

        int mark;
        long id=0;//临时存储接受到的id
        Log.d("msgProcess_login","msg.what（msgType） "+msg.what+"\nmsg携带的bundle（msgContent）内容如下\n"+content);

        /*处理不同情况的返回数据*/
        if(content.contains("@@"))//登录成功时返回数据中含有标记值和id
        {
            String [] list=content.split("@@");
            mark=Integer.valueOf(list[0]);
            id=Integer.valueOf(list[1]);
            Log.d("msg","已分配id"+String.valueOf(id));
        }
        else mark=Integer.valueOf(content);//其他情况只含有标记值
        try {
            serverSocket = new ServerSocket(localserverSocketPort);
            Log.d("ServerSocket", "ip:" + serverSocket.getInetAddress().getHostAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*处理不同的标记值*/
        switch (mark){
            case LOGIN_SUCCESS:
                Intent intent=new Intent(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),MainActivity.class);
                startActivity(intent);
                user_id=id;
                Log.d("msg_当前用户id",String.valueOf(user_id));

                finish();
                break;
            case UNREGISTER:
                Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),"账号不存在/服务器数据库连接断开",Toast.LENGTH_LONG).show();
                break;
            case PASSWORD_ERROR:
                Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),"密码错误",Toast.LENGTH_SHORT).show();
                break;
            case SERVER_READY://验证服务器状态的成功处理
                status.setText("【服务器配置】\n主服务器IP:"+mainserverIp+"\n状态:可连接");
                Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),"服务器就绪",Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),"未知错误",Toast.LENGTH_SHORT).show();
        }
    }
}