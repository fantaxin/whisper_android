package com.example.Whisper.database;
import android.content.Context;
import android.database.sqlite.*;

import androidx.annotation.Nullable;

import com.example.Whisper.define.Msg;

/*
* DBHelper 数据库帮助类，用于连接和创建数据库，同时定义了若干个SQL语句
* */

public class DBHelper extends SQLiteOpenHelper {

    public static final String CREATE_TABLE_Friends =
            "create table friends ( " +
                    "friend_id integer, " +
                    "friend_name varchar(45), " +
                    "friend_imageId integer, " +
                    "friend_type integer default 0, " +
                    //"friend_group integer default 0, " +
                    "primary key(friend_id)); " ;

    public static final String CREATE_TABLE_Friend_Online =
            "create table friends_online ( " +
                    "friend_id integer, " +
                    "friend_ip varchar(45), " +
                    "friend_port integer default 11000, " +
                    "primary key(friend_id)); ";

    public static final String CREATE_TABLE_Groups =
            "create table groups ( " +
                    "group_id integer, " +
                    "group_name varchar(45), " +
                    "group_imageId integer, " +
                    "group_ip varchar(45), " +
                    "group_port integer, " +
                    "group_description varchar(300), " +
                    "primary key (group_id)); ";

    public static final String CREATE_TABLE_GroupMember =
            "create table group_member ( " +
                    "group_id integer, " +
                    "group_member_id integer, " +
                    "group_member_online integer , " +//0不在线，1在线
                    "primary key (group_member_online)); ";

    public static final String CREATE_TABLE_UnRead_Massage =
            "";

    public static final String CREATE_TABLE_FChatFile(long friend_id){
        String sql= "create table "+"FchatFile_"+friend_id+" (" +
                    "message_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender_id integer, " +
                    "receiver_id integer, " +
                    "send_time timestamp(6) DEFAULT(datetime('now','localtime')), " +
                    //"message_type integer, " +
                    "message_read integer DEFAULT(0), " +//0未读，1已读
                    "message nvarchar(300))";
        return sql;
    }

    public static final String CREATE_TABLE_GChatFile(long group_id){
        String sql= "create table "+"GchatFile_"+group_id+" (" +
                "message_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender_id integer, " +
                "receiver_id integer, " +
                "send_time timestamp(6) DEFAULT(datetime('now','localtime')), " +
                "message_type integer, " +
                "message_read integer, " +//0未读，1已读
                "message nvarchar(300))";
        return sql;
    }

    private Context mContext;
    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    /*创建数据库默认创建好友表*/
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_Friends);
        db.execSQL(CREATE_TABLE_Friend_Online);
        db.execSQL(CREATE_TABLE_Groups);
        db.execSQL(DBHelper.CREATE_TABLE_FChatFile(Msg.PUBLIC_GROUP_ID));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
