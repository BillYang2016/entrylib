package com.billyang.entrylib.Database;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * QueryValue 类
 * 单次查询的返回类型
 */
public class QueryValue {

    @JSONField(name = "id", ordinal = 1)
    private int id;
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    @JSONField(name = "content", ordinal = 2)
    private String content;
    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}

    @JSONField(name = "time", ordinal = 3)
    private String time;
    public String getTime() {return time;}
    public void setTime(String time) {this.time = time;}

    public QueryValue() {}

    /**
     * 构造函数
     * @param id      词条id
     * @param content 词条内容
     * @param time    修改时间
     */
    public QueryValue(int id, String content, String time) {
        super();
        this.id = id;
        this.content = content;
        this.time = time;
    }
}
