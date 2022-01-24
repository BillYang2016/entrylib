package com.billyang.entrylib.Matcher;

import com.billyang.entrylib.Database.Database;
import com.billyang.entrylib.Subgroup.Subgroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.*;

/**
 * MatchValueComparator1 类
 * 比较器，对 Comparator 接口的实现
 * 使 MatchValue 的顺序为：id 第一关键字从小到大，匹配方式 type 第二关键字
 * 用于去重
 */
class MatchValueComparator1 implements Comparator<MatchValue> {
    public int compare(MatchValue a, MatchValue b) {
        if(a.getId() < b.getId()) return -1; //先按照id排序
        if(a.getId() > b.getId()) return 1;
        return Integer.compare(a.getType(), b.getType()); //再按照匹配方式排序
    }
}

/**
 * MatchValueComparator2 类
 * 比较器，对 Comparator 接口的实现
 * 使 MatchValue 的顺序为：按照优先级从小到大排序
 * 用于返回结果集
 */
class MatchValueComparator2 implements Comparator<MatchValue> {
    public int compare(MatchValue a, MatchValue b) {
        return Integer.compare(a.getPriority(), b.getPriority());
    }
}

/**
 * MatchLoader 类
 * 匹配器，通过数据库实现对词条的匹配
 * 三类匹配方法：精确（type = 0）、模糊（type = 1）、正则（type = 2）
 * @author Bill Yang
 */
public class MatchLoader {

    Database db;

    /**
     * 初始化
     * 创建新数据库对象
     */
    public void init() {
        db = new Database();
    }

    /**
     * 构造函数
     * 自动初始化
     */
    public MatchLoader() {
        init();
    }

    /**
     * 返回根据词条名匹配到的一条信息
     * 保证数据库已连接
     * 通过调用 search 函数实现
     * 开发常见问题：ResultSet 对象统一，根据数据库查询动态改变
     * @param title 词条名
     * @return 一个 MatchValue 对象
     * @see #search(String, boolean)
     * @see MatchValue
     */
    public MatchValue match(String title) {
        List<MatchValue> list = search(title, false);

        if(list.isEmpty())return new MatchValue(-1,null,-1, 0);
        else return list.get(0); //返回匹配优先级数值最小的
    }

    /**
     * 连接数据库，返回根据词条名匹配到的一条信息
     * @param name 群号或群分组
     * @param title 词条名
     * @return 一个 MatchValue 对象
     * @see #search(Object, String)
     * @see MatchValue
     */
    public MatchValue match(Object name, String title) {
        db.connect(name);

        return match(title);
    }

    /**
     * 返回根据词条名匹配到的所有信息
     * 保证数据库已连接
     * @param keyword 词条名
     * @return 一个 MatchValue 列表
     * @see MatchValue
     */
    public List<MatchValue> search(String keyword, boolean doSimilar) {
        Statement stmt = db.stmt;

        if(stmt == null) return null;

        keyword = keyword.replace("'","''"); //单引号转义

        List<MatchValue> list = new ArrayList<>();

        int id = db.find_id(keyword); //精确匹配
        if(id > 0)list.add(new MatchValue(id, keyword, 0, db.getPriority(id)));

        try {
            String sql = "SELECT * FROM __MAIN_TABLE WHERE instr('" + keyword + "',TITLE) AND MATCH_MODE=1;"; //模糊匹配
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                id = rs.getInt("ID");
                String target = rs.getString("TITLE");
                int priority = rs.getInt("PRIORITY");
                list.add(new MatchValue(id, target, 1, priority));
            }
            rs.close();
        } catch( Exception e ) {
            e.printStackTrace();
        }

        try {
            String sql = "SELECT * FROM __MAIN_TABLE WHERE MATCH_MODE=2;"; //正则匹配（sqlite不支持，Java手动实现）
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                String pattern = rs.getString("TITLE");
                boolean isMatch = Pattern.matches(pattern, keyword);
                if(isMatch) {
                    id = rs.getInt("ID");
                    int priority = rs.getInt("PRIORITY");
                    list.add(new MatchValue(id, pattern, 2, priority));
                }
            }
            rs.close();
        } catch( Exception e ) {
            e.printStackTrace();
        }

        if(doSimilar) { //限制条件
            try {
                String sql = "SELECT * FROM __MAIN_TABLE WHERE TITLE LIKE '%" + keyword + "%'"; //寻找相似词条
                ResultSet rs = stmt.executeQuery(sql);
                while(rs.next()) {
                    id = rs.getInt("ID");
                    String target = rs.getString("TITLE");
                    list.add(new MatchValue(id, target, 3, 4000));
                }
                rs.close();
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }

        list.removeIf(mv -> !db.exists(stmt, "TABLE_" + mv.getId()));
        list.removeIf(mv -> mv.getPriority() < 0);

        db.close();
        return unique(list);
    }

    /**
     * 连接数据库，返回根据词条名匹配到的所有信息
     * @param name 群号或群分组
     * @param keyword 词条名
     * @return 一个 MatchValue 列表
     * @see MatchValue
     */
    public List<MatchValue> search(Object name, String keyword) {
        db.connect(name);

        return search(keyword, true);
    }

    /**
     * 返回所有词条信息
     * 保证数据库已连接
     * @return 一个 MatchValue 列表
     * @see MatchValue
     */
    public List<MatchValue> all() {
        Statement stmt = db.stmt;

        if(stmt == null) return null;

        List<MatchValue> list = new ArrayList<>();

        try {
            String sql = "SELECT * FROM __MAIN_TABLE;";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                int id = rs.getInt("ID");
                String target = rs.getString("TITLE");
                int type = rs.getInt("MATCH_MODE");
                int priority = rs.getInt("PRIORITY");
                list.add(new MatchValue(id, target, type, priority));
            }
            rs.close();
        } catch( Exception e ) {
            e.printStackTrace();
        }

        list.removeIf(mv -> !db.exists(stmt, "TABLE_" + mv.getId()));

        db.close();
        return list;
    }

    /**
     * 连接数据库，返回所有词条信息
     * @param name 群号或群分组
     * @return 一个 MatchValue 列表
     * @see MatchValue
     */
    public List<MatchValue> all(Object name) {
        db.connect(name);

        return all();
    }

    /**
     * 对词条列表进行去重
     * 同样的词条可能有不同的匹配方式导致重复
     * @param list 词条列表
     * @return 去重后的词条列表
     */
    static List<MatchValue> unique(List<MatchValue> list) {
        if(list.isEmpty()) return list;

        list.sort(new MatchValueComparator1());

        List<MatchValue> uniqueList = new ArrayList<>();
        int lastId = -1;

        for(MatchValue mv : list) {
            if(mv.getId() != lastId) uniqueList.add(mv);
            lastId = mv.getId();
        }

        uniqueList.sort(new MatchValueComparator2());

        return uniqueList;
    }

}
