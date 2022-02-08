/*package com.example.flexiblenetworks;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/*
*服务器数据库的本地储存
**/


/*
public class MySqlHelper {
    public class DBHelper extends SQLiteOpenHelper {

        public static final String CREATE_TABLE_Register_User_List =
                "create table register_user_list ( " +
                        "id integer, " +
                        "name varchar(45), " +
                        "password varchar(45), " +
                        "primary key(id)); " ;
        public static final String CREATE_TABLE_Online_List =
                "create table online_list ( " +
                        "id integer, " +
                        "name varchar(45), " +
                        "ip varchar(45), " +
                        "port integer, " +
                        "primary key(id), " +
                        "constraint id " +
                        "foreign key (id) references register_user_list (id) " +
                        "on update cascade on delete cascade); ";
        public static final String CREATE_TABLE_Lbs =
                "create table lbs ( " +
                        "id integer, " +
                        "position varchar(300), " +
                        "constraint id " +
                        "foreign key (id) references register_user_list (id) " +
                        "on update cascade on delete cascade); ";

        public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
            super(context, name, factory, version, errorHandler);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {


        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
*/
