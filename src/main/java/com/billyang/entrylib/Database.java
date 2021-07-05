package com.billyang.entrylib;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class QueryValue {
    int id;
    String content;
    String time;

    QueryValue(int id, String content, String time) {
        this.id = id;
        this.content = content;
        this.time = time;
    }
}

public class Database {

    String RootPath;

    boolean init(String path) {
        RootPath = path;
        File file = new File("data/EntryLib/databases/");
        if(!file.exists()) {
            file.mkdirs();
        }

        return true;
    }

    public boolean exists(Statement stmt, String table) { //判断表是否存在
        try {
            stmt.executeQuery("SELECT * FROM " + table + ";");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    Connection c = null;
    Statement stmt = null;

    boolean connect(long groupId) { //连接群数据库
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:data/EntryLib/databases/" + groupId + ".db");

            stmt = c.createStatement();
            if(!exists(stmt, "__MAIN_TABLE")) { //新数据库，创建主表
                String sql = "CREATE TABLE __MAIN_TABLE " +
                             "(ID         INT           PRIMARY KEY NOT NULL," +   //编号
                             " TITLE      nvarchar(100) NOT NULL," +               //词条名
                             " MATCH_MODE INT           NOT NULL DEFAULT 0)"       //匹配模式
                        ;
                stmt.executeUpdate(sql);
            }
        } catch( Exception e ) {
            return false;
        }
        return true;
    }

    boolean close() { //关闭数据库连接
        try {
            stmt.close();
            c.close();
        } catch( Exception e ) {
            return false;
        }
        return true;
    }

    int length(String table) { //查询表项数 返回负数表示异常
        if(c == null && stmt == null)return -1;

        try {
            String sql = "SELECT count(*) as length from " + table + ";";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) return rs.getInt("length");
            else return -3;
        } catch( Exception e ) {
            return -2;
        }
    }

    int find_id(String title) { //已建立连接情况下，在主表中查询 TITLE->ID 返回负数表示异常
        if(c == null && stmt == null) return -1;

        try {
            String sql = "SELECT * FROM __MAIN_TABLE WHERE TITLE='" + title + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                int id = rs.getInt("ID");
                if(exists(stmt, "TABLE_" + id)) return id;
                else return -3;
            } else return -3; //未找到返回-3
        } catch( Exception e ) {
            return -2;
        }
    }

    boolean create_map(String title, int type) { //已建立连接情况下，新建词条表，并在主表中添加索引（需保证即将创建的表不存在）
        if(c == null && stmt == null) return false;

        try {
            int length = length("__MAIN_TABLE") + 1;

            String sql = "INSERT INTO __MAIN_TABLE (ID,TITLE,MATCH_MODE) " +
                         "VALUES (" + length + ",'" + title + "'," + type + ");";
            stmt.executeUpdate(sql); //主表添加索引

            sql = "CREATE TABLE TABLE_" + length +
                  "(ID         INT            PRIMARY KEY NOT NULL," +                          //版本号
                  " CONTENT    nvarchar(1000) ," +                                              //内容
                  " TS         TIMESTAMP      NOT NULL DEFAULT (datetime('now','localtime')))"  //时间
            ;
            stmt.executeUpdate(sql); //创建新表

        } catch( Exception e ) {
            return false;
        }

        return true;
    }

    boolean insert(long groupId, String title, String content, int type, StringBuilder ErrorInfo) { //向title词条插入新内容content，返回错误信息ErrorInfo
        if(!connect(groupId)) {
            ErrorInfo.append("数据库连接失败！");
            return false;
        }

        int id = find_id(title);
        if(id == -3)
            if(!create_map(title, type)) {
                close();
                ErrorInfo.append("无法创建新表！");
                return false;
            } else id = length("__MAIN_TABLE");
        else if(id < 0) {
            close();
            ErrorInfo.append("数据库查询异常！");
            return false;
        }

        String table = "TABLE_" + id;
        int length = length(table) + 1;

        try {
            String sql = "INSERT INTO " + table + " (ID,CONTENT)" +
                         "VALUES (" + length + ",'" + content +"');"
                    ;
            stmt.executeUpdate(sql);
        } catch( Exception e ) {
            close();
            ErrorInfo.append("无法向").append(table).append("词条表中插入数据！");
            return false;
        }

        close();
        return true;
    }

    boolean delete(long groupId, String title, StringBuilder ErrorInfo) { //向title词条插入新内容content，返回错误信息ErrorInfo
        if(!connect(groupId)) {
            ErrorInfo.append("数据库连接失败！");
            return false;
        }

        int id = find_id(title);
        if(id == -3) {
            close();
            ErrorInfo.append(title).append(" 词条不存在！");
            return false;
        } else if(id < 0) {
            close();
            ErrorInfo.append("数据库查询异常！");
            return false;
        }

        String table = "TABLE_" + id;

        try {
            String sql = "DROP TABLE " + table + ";";
            stmt.executeUpdate(sql);
        } catch( Exception e ) {
            close();
            ErrorInfo.append("无法删除").append(table).append("词条表！");
            return false;
        }

        close();
        return true;
    }

    String query(long groupId, int id, StringBuilder ErrorInfo) {
        if(!connect(groupId)) {
            ErrorInfo.append("数据库连接失败！");
            return null;
        }

        String table = "TABLE_" + id;
        int length = length(table);

        try {
            String sql = "SELECT * FROM " + table + " WHERE ID=" + length + ";";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                String content = rs.getString("CONTENT");
                close();
                return content;
            } else {
                close();
                ErrorInfo.append("无法在").append(table).append("词条表中找到最新记录！");
                return null;
            }
        } catch( Exception e ) {
            close();
            ErrorInfo.append("无法在").append(table).append("词条表中查询数据！");
            return null;
        }
    }

    List<QueryValue> history(long groupId, int id, StringBuilder ErrorInfo) {
        if(!connect(groupId)) {
            ErrorInfo.append("数据库连接失败！");
            return null;
        }

        String table = "TABLE_" + id;
        List<QueryValue> list = new ArrayList<>();

        try {
            String sql = "SELECT * FROM " + table + ";";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                int versionId = rs.getInt("ID");
                String content = rs.getString("CONTENT");
                String time = rs.getString("TS");

                QueryValue qv = new QueryValue(versionId, content, time);
                list.add(qv);
            }
        } catch( Exception e ) {
            close();
            ErrorInfo.append("无法在").append(table).append("词条表中查询数据！");
            return null;
        }

        close();
        return list;
    }
}
