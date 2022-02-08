package com.example.Whisper.define;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
/*活动收集器，保存所有打开且未被销毁的活动，用来退出程序时，一键关闭所有活动*/
public class ActivityCollector {//什么时候被实例化的？似乎是被调用时自动初始化
    public static List<Activity> activities=new ArrayList<>();
    public static void addActivity(Activity activity){
        activities.add(activity);
    }
    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }
    /*销毁所有活动*/
    public static void finishAll(){
        for(Activity activity:activities){
            activity.finish();
        }
    }
}
