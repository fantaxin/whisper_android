package com.example.Whisper.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Whisper.define.Msg;
import com.example.Whisper.R;

import java.util.List;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder>{
    private List<Msg> mMsgList;
    /*MsgAdapter的内部类*/
    static class ViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        TextView leftName;
        public ViewHolder(@NonNull View itemView) {//该参数为子项的最外层布局文件
            super(itemView);
            leftLayout=(RelativeLayout)itemView.findViewById(R.id.left_layout);
            rightLayout=(LinearLayout)itemView.findViewById(R.id.right_layout);
            leftMsg=(TextView) itemView.findViewById(R.id.left_msg);
            rightMsg=(TextView)itemView.findViewById(R.id.right_msg);
            leftName=(TextView)itemView.findViewById(R.id.sender_name);
        }
    }

    /*RecyclerView的构造函数，消息列表传入适配器*/
    public MsgAdapter(List<Msg>msgList){
        mMsgList=msgList;
    }
    /*创建ViewHolder实例*/
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item,parent,false);//将子项的布局加载进来
        return new ViewHolder(view);//并用此布局构建一个Viewholder实例
    }
    /*对子项数据进行赋值，会在每个子项被滚动到屏幕内的时候执行*/
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {//此处的holder即通过onCreateViewHolder构建出来的
        Msg msg=mMsgList.get(position);
        if(msg.getType()==Msg.TYPE_RECEIVE){
            holder.leftLayout.setVisibility((View.VISIBLE));
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
            holder.leftName.setText(msg.getSender_name());
        }else if(msg.getType()==Msg.TYPE_SENT){
            holder.rightLayout.setVisibility((View.VISIBLE));
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightMsg.setText(msg.getContent());
        }
        Log.d("mark1",msg.getContent());
    }
    /*RecyclerView的项数*/
    @Override
    public int getItemCount() {
        return mMsgList.size();
    }

}
