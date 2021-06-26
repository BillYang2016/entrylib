package com.billyang.entrylib;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;

import java.io.File;
import java.sql.*;

public class Database {

    boolean init() {
        File file = new File("data/EntryLib/databases/");
        if(!file.exists()) {
            file.mkdirs();
        }

        return true;
    }

    public boolean exists(Statement stmt,String table) { //判断表是否存在
        try {
            stmt.executeQuery("SELECT * FROM " + table);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    Connection c = null;
    Statement stmt = null;

    boolean connect(long groupid) { //连接群数据库
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:data/EntryLib/databases/" + groupid + ".db");

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
            if(rs.next())return rs.getInt("length");
            else return -3;
        } catch( Exception e ) {
            return -2;
        }
    }

    int find_id(String title) { //已建立连接情况下，在主表中查询 TITLE->ID 返回负数表示异常
        if(c == null && stmt == null)return -1;

        try {
            String sql = "SELECT * FROM __MAIN_TABLE WHERE TITLE='" + title + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next())return rs.getInt("ID");
            else return -3; //未找到返回-3
        } catch( Exception e ) {
            return -2;
        }
    }

    boolean create_map(String title,int type) { //已建立连接情况下，新建词条表，并在主表中添加索引（需保证即将创建的表不存在）
        if(c == null && stmt == null)return false;

        try {
            int length = length("__MAIN_TABLE") + 1;

            String sql = "INSERT INTO __MAIN_TABLE (ID,TITLE,MATCH_MODE) " +
                         "VALUES (" + length + ",'" + title + "'," + type + ");";
            stmt.executeUpdate(sql); //主表添加索引

            sql = "CREATE TABLE TABLE_" + length +
                  "(ID         INT            PRIMARY KEY NOT NULL," +                                  //版本号
                  " CONTENT    nvarchar(1000) ," +                                             //内容
                  " TS         TIMESTAMP      NOT NULL DEFAULT (datetime('now','localtime')))" //时间
            ;
            stmt.executeUpdate(sql); //创建新表

        } catch( Exception e ) {
            return false;
        }

        return true;
    }

    boolean insert(long groupid,String title,String content,int type,StringBuilder ErrorInfo) {
        if(!connect(groupid)) {
            ErrorInfo.append("数据库连接失败！");
            return false;
        }

        int id = find_id(title);
        if(id == -3)
            if(!create_map(title,type)) {
                close();
                ErrorInfo.append("无法创建新表！");
                return false;
            } else id = length("__MAIN_TABLE");
        else if(id < 0) {
            close();
            ErrorInfo.append("数据库查询异常！");
            return false;
        }

        String table = "TABLE_"+id;
        int length = length(table) + 1;

        try {
            String sql = "INSERT INTO " + table + " (ID,CONTENT)" +
                         "VALUES (" + length + ",'" + content +"');"
                    ;
            stmt.executeUpdate(sql);
        } catch( Exception e ) {
            close();
            ErrorInfo.append("无法向" + table + "词条表中插入数据！");
            return false;
        }

        close();
        return true;
    }

    String query(String title) {
        return null;
    }

    String history(String title) {
        return null;
    }

}
