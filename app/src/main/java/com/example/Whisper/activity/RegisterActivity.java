package com.example.Whisper.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.Whisper.define.ActivityCollector;
import com.example.Whisper.define.Msg;
import com.example.Whisper.R;

public class RegisterActivity extends BaseActivity implements View.OnClickListener{
    private EditText name;
    private EditText passwordEdit;
    private EditText surePassword;
    private Button sure;
    private Button cancel;
    private SharedPreferences.Editor editor;
    private SharedPreferences pref;

    public RegisterActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_register);
        tcp_sender.setHandler(handler);
/*        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null) actionBar.hide();*/
        Init();
    }

    private void Init() {
        this.name = (EditText)this.findViewById(R.id.username);
        this.passwordEdit = (EditText)this.findViewById(R.id.password);
        this.surePassword = (EditText)this.findViewById(R.id.sure_password);
        this.cancel = (Button)this.findViewById(R.id.cancelRegister);
        this.sure = (Button)this.findViewById(R.id.addRegister);
        this.sure.setOnClickListener(RegisterActivity.this);
        this.cancel.setOnClickListener(RegisterActivity.this);
        this.pref= getSharedPreferences("login", Context.MODE_PRIVATE);
    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addRegister:
            {
                String account=name.getText().toString().trim();
                String password=passwordEdit.getText().toString();
                String password2=surePassword.getText().toString();
                if(account.isEmpty()||password.isEmpty())
                {
                    Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),"????????????????????????",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(!password.equals(password2)){
                    Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),"??????????????????????????????",Toast.LENGTH_SHORT).show();
                    return;
                }
                for(int i = 0; i<password.length(); i++) {
                    char word = password.charAt(i);
                    if (word == '_')
                        continue;
                    if (word < 48 || (word > 57 && word < 65) || (word > 90 && word < 97) || word > 122) {
                        Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size() - 1),
                                "?????????????????????????????????????????????????????????", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                editor=pref.edit();

                editor.clear();
                editor.apply();
                Log.d("local","apply????????????preference");
/*                if(autoLogin.isChecked()){
                    editor.putBoolean("auto_login",true);
                }else {
                    editor.clear();
                }
                editor.apply();*/

                /*??????????????????????????????*/
                Msg msg=new Msg(Msg.TYPE_REGISTER,0,account+"@@"+password);
                Log.d("msg","??????????????????");
                tcp_sender.putMsg(msg);
                tcp_sender_tread.interrupt();//?????????????????????
                break;
            }
            case R.id.cancelRegister:
                finish();
                break;
        }

    }

    @Override
    public void processMessage(Message msg) {
        Log.d("pro","????????????????????????");
        String content=msg.getData().getString("content");
        int mark;
        long id=0;
        Log.d("msgProcess_login","msg.what???msgType??? "+msg.what+"\nmsg?????????bundle???msgContent???????????????\n"+content);

        /*?????????????????????????????????*/
        if(content.contains("@@"))//????????????????????????????????????????????????id
        {
            String [] list=content.split("@@");
            mark=Integer.valueOf(list[0]);
            id=Integer.valueOf(list[1]);
            Log.d("msg","?????????id"+String.valueOf(id));
        }
        else mark=Integer.valueOf(content);//??????????????????????????????

        /*????????????????????????*/
        switch (mark){
            /*case Msg.REGISTER_SUCCESS:
                Intent intent=new Intent(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),MainActivity.class);
                startActivity(intent);
                user_id=id;
                Log.d("msg_????????????id",String.valueOf(user_id));
                finish();
                break;*/
            case Msg.REGISTER_SUCCESS:
                Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),"????????????",Toast.LENGTH_SHORT).show();
                finish();
                break;
            case Msg.REGISTER_DEFAULT:
                Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),"?????????????????????",Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),"????????????",Toast.LENGTH_SHORT).show();
        }
    }
}
