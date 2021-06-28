package com.billyang.entrylib;

import java.sql.*;
import java.util.regex.*;

class MatchValue {
    int id;
    String title;
    int type;
    MatchValue(int id,String title,int type) {
        this.id = id;
        this.title = title;
        this.type = type;
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

    String search(String keyword) {
        return null;
    }

}
