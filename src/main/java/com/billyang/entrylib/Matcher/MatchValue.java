package com.billyang.entrylib.Matcher;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * MatchValue 类
 * 单次匹配的返回类型
 */
public class MatchValue {
    private int id;
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    private String title;
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    private int type;
    public int getType() {return type;}
    public void setType(int type) {this.type = type;}

    private int priority;
    public int getPriority() {return priority;}
    public void setPriority(int priority) {this.priority = priority;}

    /**
     * 构造函数
     *
     * @param id    词条id
     * @param title 词条名
     * @param type  匹配方式
     * @param priority 优先级
     */
    MatchValue(int id, String title, int type, int priority) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.priority = priority;
    }
}
