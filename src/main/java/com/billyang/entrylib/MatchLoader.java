package com.billyang.entrylib;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.*;

class MatchValue {
    int id;
    String title;
    int type;

    MatchValue(int id, String title, int type) {
        this.id = id;
        this.title = title;
        this.type = type;
    }
}

class MatchValueComparator implements Comparator<MatchValue> {
    public int compare(MatchValue a, MatchValue b) {
        if(a.id < b.id) return -1; //先按照id排序
        if(a.id > b.id) return 1;
        if(a.type < b.type) return -1; //再按照查找方式排序
        if(a.type > b.type) return 1;
        return 0;
    }
}

public class MatchLoader {

    Database db;

    void init(Database db) {
        this.db=db;
    }

    MatchValue match(long groupID, String title) { //返回title匹配到的词条表id与匹配类型
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
            if(rs.next()) {
                id = rs.getInt("ID");
                String target = rs.getString("TITLE");
                db.close();
                return new MatchValue(id, target, 1);
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }

        try {
            String sql = "SELECT * FROM __MAIN_TABLE WHERE MATCH_MODE=2;"; //正则匹配（sqlite不支持，Java手动实现）
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                String pattern = rs.getString("TITLE");
                boolean isMatch = Pattern.matches(pattern, title);
                if(isMatch) {
                    id = rs.getInt("ID");
                    db.close();
                    return new MatchValue(id, pattern, 2);
                }
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }

        db.close();
        return new MatchValue(-1,null,-1);
    }

    List<MatchValue> search(long groupID, String keyword) {
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
        } catch( Exception e ) {
            e.printStackTrace();
        }

        db.close();
        return unique(list);
    }

    List<MatchValue> all(long groupID) {
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
        } catch( Exception e ) {
            e.printStackTrace();
        }

        return list;
    }

    static List<MatchValue> unique (List<MatchValue> list) {
        if(list.isEmpty()) return list;

        Collections.sort(list,new MatchValueComparator());

        List<MatchValue> uniqueList = new ArrayList<>();
        int lastid = -1;

        for(MatchValue mv : list) {
            if(mv.id != lastid) uniqueList.add(mv);
            lastid = mv.id;
        }

        return uniqueList;
    }

}
