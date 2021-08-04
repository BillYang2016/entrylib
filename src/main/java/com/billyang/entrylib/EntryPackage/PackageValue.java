package com.billyang.entrylib.EntryPackage;

import com.alibaba.fastjson.annotation.JSONField;
import com.billyang.entrylib.Database.QueryValue;

import java.util.List;

/**
 * PackageValue 类
 * 词条包单项词条内容
 * 用于 json 反序列
 */
public class PackageValue {

    @JSONField(name = "title", ordinal = 1)
    private String title;
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    @JSONField(name = "mode", ordinal = 2)
    private int mode;
    public int getMode() {return mode;}
    public void setMode(int mode) {this.mode = mode;}

    @JSONField(name = "priority", ordinal = 3)
    private int priority;
    public int getPriority() {return priority;}
    public void setPriority(int priority) {this.priority = priority;}

    @JSONField(name = "history", ordinal = 4)
    private List<QueryValue> history;
    public List<QueryValue> getHistory() {return history;}
    public void setHistory(List<QueryValue> history) {this.history = history;}

    public PackageValue() {}

    /**
     * 构造函数
     *
     * @param title   词条名
     * @param mode    匹配模式
     * @param history 历史情况
     */
    public PackageValue(String title, int mode, List<QueryValue> history) {
        super();
        this.title = title;
        this.mode = mode;
        this.priority = 2000;
        this.history = history;
    }

    /**
     * 构造函数
     *
     * @param title   词条名
     * @param mode    匹配模式
     * @param priority 匹配方式
     * @param history 历史情况
     */
    public PackageValue(String title, int mode, int priority, List<QueryValue> history) {
        super();
        this.title = title;
        this.mode = mode;
        this.priority = priority;
        this.history = history;
    }

}
