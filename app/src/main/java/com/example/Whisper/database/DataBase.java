package com.example.Whisper.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Message;
import android.util.Log;

import com.example.Whisper.activity.BaseActivity;
import com.example.Whisper.define.Friend;
import com.example.Whisper.define.Friend_Message;
import com.example.Whisper.define.Msg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
/*
* DataBase类，封装数据库操作函数
* 注意：
* 1、创建DataBase实例：DataBase dataBase = new DataBase(this);
* 2、调用DataBase中的函数：dataBase.xxx(x,...);
* 3、删除db.setTransactionSuccessful();
* 4、可以在BaseActivity中创建DataBase实例
* 5、调用add_ChatFile前先创建好友聊天数据库（调用add_Friend方法创建）
* 6、可以在FriendChatActivity的OnItemClick方法中调用add_Friend方法 BaseActivity.dataBase.add_friend(Friend.getId(), Friend.getName(), Friend.getImageId());
* 7、可以在MsgActivity的send按钮的OnClick事件中调用add_ChatFile方法 dataBase.add_chatFile(user_id, chat_aim.getId(), content);
* */

public class DataBase extends BaseActivity {

    protected DBHelper dbHelper;
    private static SQLiteDatabase db;

    /*创建DBHelper实例为新用户创建数据库*/
    public DataBase(Context context)
    {
        dbHelper = new DBHelper(context,"userDB_"+BaseActivity.user_id+".db",null,1);
    }

    /*打开数据库，之后可以在Android studio中访问数据库*/
    public void openDataBase()
    {
        db = dbHelper.getWritableDatabase();//创建数据库对象
    }

    /*关闭数据库，使用openDataBase()函数后要记得调用该函数*/
    public void closeDataBase()
    {
        db.close();
    }

    /*添加好友*/
    public void add_friend(long friend_id, String friend_name, int friend_imageId)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("friend_id", friend_id);
            values.put("friend_name", friend_name);
            values.put("friend_imageId", friend_imageId);
            db.insert("friends", null, values);
            db.execSQL(DBHelper.CREATE_TABLE_FChatFile(friend_id));//自动新建一个聊天记录表
            //db.setTransactionSuccessful();
        }catch (SQLiteException e) {
            //db.close();
            e.printStackTrace();
        }
        //db.close();
    }

    /*添加聊天记录，参数：发送者id、接收者id、信息，发送者和接收者中有一个为User*/
    public void add_chatFile(long sender_id, long receiver_id, String message)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("sender_id", sender_id);
            values.put("receiver_id", receiver_id);
            values.put("message", message);
            values.put("message_read",false);
            long friend_id;
            if (sender_id == user_id)
                friend_id = receiver_id;
            else
                friend_id = sender_id;
            db.insert("FchatFile_" + friend_id, null, values);
            //db.setTransactionSuccessful();
        }catch (SQLiteException e) {
            //db.close();
            e.printStackTrace();
        }
        //db.close();
    }

    public void add_public_chatFile(long sender_id, long receiver_id, String message)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("sender_id", sender_id);
            values.put("receiver_id", receiver_id);
            values.put("message", message);
            values.put("message_read",false);
            db.insert("FchatFile_" + Msg.PUBLIC_GROUP_ID, null, values);
            //db.setTransactionSuccessful();
        }catch (SQLiteException e) {
            //db.close();
            e.printStackTrace();
        }
        //db.close();
    }

    /*删除好友，参数：好友id、是否删除聊天记录（可以不写，默认为不删除）*/
    public void delete_friend(long friend_id, boolean deleteChatFile)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.delete("friends", "friend_id=?", new String[]{friend_id + ""});
            if (deleteChatFile)
            {
                db.execSQL("drop table " + "FchatFile_" + friend_id);
            }
        } catch (SQLiteException e) {
            //db.close();
            e.printStackTrace();
        }
        //db.close();
    }

    /*删除好友，参数：好友id、是否删除聊天记录（可以不写，默认为不删除）*/
    public void delete_friend(long friend_id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try
        {
            boolean deleteChatFile = false;
            db.delete("friends", "friend_id=?", new String[]{friend_id + ""});
            if (deleteChatFile)
            {
                db.execSQL("drop table " + "FchatFile_" + friend_id);
            }
        } catch (SQLiteException e) {
            //db.close();
            e.printStackTrace();
        }
        //db.close();
    }

    /*更新消息读取记录*/
    public void update_message_onRead(long friend_id, int message_id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("message_read", 1);
            db.update("FchatFile_" + friend_id, values, "message_id = ?", new String[]{String.valueOf(message_id)});
        } catch (SQLiteException e) {
            //db.close();
            e.printStackTrace();
        }
    }
    /*查询聊天记录，结果以List储存，参数：朋友id、已查询聊天记录数（0，10，20...）*/
    public List<Friend_Message> search_chatFile(long friend_id, int num) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        LinkedList<Friend_Message> result_chatFiles = new LinkedList<Friend_Message>();
        try {
            Cursor cursor = db.query("FchatFile_" + friend_id, null, null, null, null, null, "send_time desc");
            //cursor.moveToLast();
            //cursor.moveToFirst();
            //int last_position = cursor.getPosition();//将指针移到表的末尾
            //if (cursor.moveToPosition(last_position-num)) {//将指针移到上次读取的位置
            cursor.moveToPosition(num-1);//上次查询的位置
                while (cursor.moveToNext()){
                    //每次查询只查询10条数据
                    if(cursor.getPosition()==num+4)
                        break;
                    Friend_Message result;
                    int message_id = cursor.getInt(cursor.getColumnIndex("message_id"));
                    Log.d("database:", String.valueOf(message_id));
                    long sender_id = cursor.getInt(cursor.getColumnIndex("sender_id"));
                    long receiver_id = cursor.getInt(cursor.getColumnIndex("receiver_id"));
                    String send_time = cursor.getString(cursor.getColumnIndex("send_time"));
                    String message = cursor.getString(cursor.getColumnIndex("message"));
                    result = new Friend_Message(true, sender_id, receiver_id, send_time, message);
                    result_chatFiles.add(result);
                    //update_message_onRead(friend_id,message_id);//将消息置为已读
                } //如果已查询完全部信息或本次已查询10条信息
            //}
            cursor.close();//查询完释放cursor
        } catch (SQLiteException e) {
            //db.close();
            e.printStackTrace();
        }
        //db.close();//关闭数据库
        return result_chatFiles;
    }

    /*查找好友，结果以List储存，返回全部好友*/
    public List<Friend>  search_friends() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<Friend> result_Friends = new ArrayList<Friend>();
        try {
            Cursor cursor = db.query("friends", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    Friend result;
                    long friend_id = cursor.getInt(cursor.getColumnIndex("friend_id"));
                    String friend_name = cursor.getString(cursor.getColumnIndex("friend_name"));
                    int friend_imageId = cursor.getInt(cursor.getColumnIndex("friend_imageId"));
                    int friend_group = cursor.getInt(cursor.getColumnIndex("friend_group"));
                    result = new Friend(friend_id,friend_name,friend_imageId);
                    result_Friends.add(result);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLiteException e) {
            //db.close();
            e.printStackTrace();
        }
        //db.close();
        return result_Friends;
    }

    /*更新好友昵称*/
    public void update_friends_name(long friend_id, String new_friend_name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("friend_name", new_friend_name);
            db.update("friends", values, "friend_id = ?", new String[]{String.valueOf(friend_id)});
        } catch (SQLiteException e) {
            //db.close();
            e.printStackTrace();
        }
        //db.close();
    }

    /*更新好友头像*/
    public void update_friend_imageId(long friend_id, int new_friend_imageId)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("friend_imageId", new_friend_imageId);
            db.update("friends", values, "friend_id = ?", new String[]{String.valueOf(friend_id)});
        } catch (SQLiteException e) {
            //db.close();
            e.printStackTrace();
        }
        //db.close();
    }

    @Override
    public void processMessage(Message msg) {

    }
}
