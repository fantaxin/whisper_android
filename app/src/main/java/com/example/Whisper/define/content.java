package com.example.Whisper.define;
/*
* 交易市场内容格式
* */
public class content {
    private String name;
    private int imageId;
    public content(String name,int imageId)
    {
        this.name=name;
        this.imageId=imageId;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
