package com.example.Whisper.define;
/*未读消息类，存储各种未读的消息，存储查看详情所需的必备信息
* 私聊消息，需有对方id，昵称，ip
* 群聊消息，群聊id
* 故需要设置，消息类型(群聊，私聊，交易，好友申请等)，主索引项id(好友id，群聊id)，
* 辅助索引ip(私聊ip，群聊服务器ip)，描述信息（图片，当前为固定图片），描述信息(文字)，未读消息数量
* 通过主索引可以在概述数据库中表(注册列表，好友列表，群列表，未读概述）中找到辅助索引，描述信息...
* 后期增加
* 其他描述信息，如头像，预计本地以文件形式缓存好友头像
* 注意，listview中的项需要与数据库存储尽量对应，方便存储
*
* Msg 构造时type，发送者id，content， 根据接收者id，在好友表中查询，如果无则查询服务器，服务器也无则由服务器暂存 目标ip，port
* 传输时只有type，发送者id，content
* 解析时只有type，发送者id，content，但手动加上空ip和port
* 存储时 type，发送者id，content
* --应该均加上接收者id，构造时间
*
*
* friend id，昵称，头像（当前固定），ip，port（默认11000），在线状态（0，非0），优先级（默认0，
* 表示不是好友，对方同意后置为5，非0则为好友）
* 后期增加
* 备注，优先级，个人基本信息
*
* 每次登录时需要更新好友列表（发送查询包，含有要查询的id列表，服务端按照id ip 在线状态的形式返回数据）
* 每隔2分钟重新获取，同时充当心跳，表明自己在线
* 服务端记录变量初始值2，每隔2分钟遍历一次数据库，将注册列表中的值减1，为0则不再减，表示离线，如果受到请求则重新置为2
* 如此可保证非正常下线时最迟4分钟可以发现。
*
*
* 注册列表，含有id，昵称，头像
* 可根据昵称或者id增加好友，先将要添加的人放入好友表，然后向服务端发送请求
* 发送消息将首先判断优先级，为0则需加为好友，否则从本地好友库中判断在线状态，查询ip，决定发送方式
* 默认策略为，可见所有群聊，所有注册用户，但需加为好友方可聊天。
*
* group id，服务器ip，成员列表，描述信息
*
*
* */
public class messageItem {
    private String name;
    private int ImageId;
    private String partMessage;
    public Friend friend;
    public messageItem(String name,int imageId,String partMessage){
        this.name=name;
        this.ImageId=imageId;
        this.partMessage=partMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Friend getFriend() {
        return friend;
    }

    public void setFriend(Friend friend) {
        this.friend = friend;
    }

    public int getImageId() {
        return ImageId;
    }

    public void setImageId(int imageId) {
        ImageId = imageId;
    }

    public String getPartMessage() {
        return partMessage;
    }

    public void setPartMessage(String partMessage) {
        this.partMessage = partMessage;
    }
}
