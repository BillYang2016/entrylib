package com.billyang.entrylib.Subgroup;

import java.util.List;

/**
 * Subgroup 类
 * 提供群分组所需的小组对象
 */
public class Subgroup {
    String name;
    List<Long> groupList;

    /**
     * 构造函数
     * @param name 小组名
     * @param groupList 小组成员，由群号组成的列表
     */
    public Subgroup(String name, List<Long> groupList) {
        this.name = name;
        this.groupList = groupList;
    }

    /**
     * 获取群分组的小组名
     * @return 小组名
     */
    public String getName() {return name;}
}
