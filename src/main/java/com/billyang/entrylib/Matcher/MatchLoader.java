package com.billyang.entrylib.Matcher;

import com.billyang.entrylib.Database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.*;

/**
 * MatchValueComparator 类
 * 比较器，对 Comparator 接口的实现
 * 使 MatchValue 的顺序为：id 第一关键字从小到大，匹配方式 type 第二关键字
 */
class MatchValueComparator implements Comparator<MatchValue> {
    public int compare(MatchValue a, MatchValue b) {
        if(a.getId() < b.getId()) return -1; //先按照id排序
        if(a.getId() > b.getId()) return 1;
        return Integer.compare(a.getType(), b.getType()); //再按照匹配方式排序
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
     * @param db 指定数据库
     */
    public void init(Database db) {
        this.db=db;
    }

    /**
     * 连接群数据库，返回根据词条名匹配到的一条信息
     * 优化方向：可以直接调用 search，但会降低效率
     * 开发常见问题：ResultSet 对象统一，根据数据库查询动态改变
     * @param groupID 群号
     * @param title 词条名
     * @return 一个 MatchValue 对象
     * @see #search(long, String)
     * @see MatchValue
     */
    public MatchValue match(long groupID, String title) {
        db.connect(groupID);

        Statement stmt = db.stmt;

        int id = db.find_id(title); //精确匹配
        if(id > 0) {
            db.close();
            return new MatchValue(id, title, 0);
        }

        try {
            String sql = "SELECT * FROM __MAIN_TABLE WHERE instr('" + title + "',TITLE) AND MATCH_MODE=1;"; //模糊匹配
            ResultSet rs = stmt.executeQuery(sql);
            List<MatchValue> list = new ArrayList<>();

            while(rs.next()) {
                id = rs.getInt("ID");
                String target = rs.getString("TITLE");
                list.add(new MatchValue(id, target, 1));
            }
            rs.close();

            for(MatchValue mv : list) {
                if(db.exists(stmt, "TABLE_" + mv.getId())) {
                    db.close();
                    return mv;
                }
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }

        try {
            String sql = "SELECT * FROM __MAIN_TABLE WHERE MATCH_MODE=2;"; //正则匹配（sqlite不支持，Java手动实现）
            ResultSet rs = stmt.executeQuery(sql);
            List<MatchValue> list = new ArrayList<>();

            while(rs.next()) {
                String pattern = rs.getString("TITLE");
                boolean isMatch = Pattern.matches(pattern, title);
                if(isMatch) {
                    id = rs.getInt("ID");
                    list.add(new MatchValue(id, pattern, 2));
                }
            }
            rs.close();

            for(MatchValue mv : list) {
                if(db.exists(stmt, "TABLE_" + mv.getId())) {
                    db.close();
                    return mv;
                }
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }

        db.close();
        return new MatchValue(-1,null,-1);
    }

    /**
     * 连接群数据库，返回根据词条名匹配到的所有信息
     * @param groupID 群号
     * @param keyword 词条名
     * @return 一个 MatchValue 列表
     * @see MatchValue
     */
    public List<MatchValue> search(long groupID, String keyword) {
        db.connect(groupID);

        Statement stmt = db.stmt;

        List<MatchValue> list = new ArrayList<>();

        int id = db.find_id(keyword); //精确匹配
        if(id > 0)list.add(new MatchValue(id, keyword, 0));

        try {
            String sql = "SELECT * FROM __MAIN_TABLE WHERE instr('" + keyword + "',TITLE) AND MATCH_MODE=1;"; //模糊匹配
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                id = rs.getInt("ID");
                String target = rs.getString("TITLE");
                list.add(new MatchValue(id, target, 1));
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
                    list.add(new MatchValue(id, pattern, 2));
                }
            }
            rs.close();
        } catch( Exception e ) {
            e.printStackTrace();
        }

        try {
            String sql = "SELECT * FROM __MAIN_TABLE WHERE TITLE LIKE '%" + keyword + "%'"; //寻找相似词条
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                id = rs.getInt("ID");
                String target = rs.getString("TITLE");
                list.add(new MatchValue(id, target, 3));
            }
            rs.close();
        } catch( Exception e ) {
            e.printStackTrace();
        }

        list.removeIf(mv -> !db.exists(stmt, "TABLE_" + mv.getId()));

        db.close();
        return unique(list);
    }

    /**
     * 连接群数据库，返回所有词条信息
     * @param groupID 群号
     * @return 一个 MatchValue 列表
     * @see MatchValue
     */
    public List<MatchValue> all(long groupID) {
        db.connect(groupID);

        Statement stmt = db.stmt;

        List<MatchValue> list = new ArrayList<>();

        try {
            String sql = "SELECT * FROM __MAIN_TABLE;";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                int id = rs.getInt("ID");
                String target = rs.getString("TITLE");
                int type = rs.getInt("MATCH_MODE");
                list.add(new MatchValue(id, target, type));
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
     * 对词条列表进行去重
     * 同样的词条可能有不同的匹配方式导致重复
     * @param list 词条列表
     * @return 去重后的词条列表
     */
    static List<MatchValue> unique (List<MatchValue> list) {
        if(list.isEmpty()) return list;

        list.sort(new MatchValueComparator());

        List<MatchValue> uniqueList = new ArrayList<>();
        int lastId = -1;

        for(MatchValue mv : list) {
            if(mv.getId() != lastId) uniqueList.add(mv);
            lastId = mv.getId();
        }

        return uniqueList;
    }

}
