package com.example.final_task;

import java.io.Serializable;

public class memo implements Serializable {
    private int id;//id
    private String title;//备忘信息标题
    private String date;//修改日期
    private String content;//备忘信息全部内容
    private String memoType;//备忘信息分组
    private String alarm_date;//提醒时间

    public memo() {
    }
    public memo(String title, String date) {
        this.title = title;
        this.date = date;
    }
    public memo(int id,String title, String content,String date ) {
        this.title = title;
        this.date = date;
        this.id = id;
        this.content=content;
    }
    public memo(int id,String title, String content,String date,String memoType,String alarm_date) {
        this.title = title;
        this.date = date;
        this.id = id;
        this.content=content;
        this.memoType=memoType;
        this.alarm_date=alarm_date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMemoType() {
        return memoType;
    }

    public void setMemoType(String memoType) {
        this.memoType = memoType;
    }

    public String getAlarm_date() {
        return alarm_date;
    }

    public void setAlarm_date(String alarm_date) {
        this.alarm_date = alarm_date;
    }



}
