package com.billyang.entrylib.Database;

import com.billyang.entrylib.EntryLib;
import com.billyang.entrylib.Subgroup.Subgroup;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Database 类
 * 实现对数据库的控制
 * 包含基础控制方法与指定查询方法
 * 模糊查询由 MatchLoader 类进行实现
 * @author Bill Yang
 */
public class Database {

    /**
     * 初始化
     * @return 成功状态
     */
    public boolean init() {
        File file = new File(EntryLib.DATABASES_FOLDER);
        if(!file.exists()) {
            EntryLib.INSTANCE.getLogger().info("创建数据库目录");
            file.mkdirs();
        }

        return true;
    }

    /**
     * 构造函数
     * 自动初始化
     */
    public Database() {
        init();
    }

    /**
     * 查询数据库中某个表是否存在
     * @param stmt 数据库 Statement 对象
     * @param table 表名
     * @return 表的存在性
     */
    public boolean exists(Statement stmt, String table) {
        try {
            stmt.executeQuery("SELECT * FROM " + table + ";");
            return true;
        } catch (SQLException e) {
            //e.printStackTrace();
            return false;
        }
    }

    Connection c = null;
    public Statement stmt = null;

    /**
     * 查询数据库中某个表中某个列是否存在
     * @param table 表名
     * @param column 列名
     * @return 列的存在性
     */
    boolean exists(String table, String column) {
        String sql = "select sql from sqlite_master where tbl_name='" + table + "' and type='table';";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                String content = rs.getString("sql");
                return content.contains(column);
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 连接数据库
     * @param database 数据库
     * @return 连接情况
     */
    public boolean connect(String database) {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(database);

            stmt = c.createStatement();
            if(!exists(stmt, "__MAIN_TABLE")) { //新数据库，创建主表
                String sql = "CREATE TABLE __MAIN_TABLE " +
                        "(ID         INT           PRIMARY KEY NOT NULL," +   //编号
                        " TITLE      nvarchar(100) NOT NULL," +               //词条名
                        " MATCH_MODE INT           NOT NULL DEFAULT 0," +     //匹配模式
                        " PRIORITY   INT           NOT NULL DEFAULT 2000)"    //优先级
                        ;
                stmt.executeUpdate(sql);

                sql = "CREATE TABLE __VERSION (VERSION         INT            PRIMARY KEY NOT NULL)"; //创建版本表
                stmt.executeUpdate(sql);

                sql = "INSERT INTO __VERSION (VERSION) VALUES (" + DatabaseUpdater.VERSION + ");"; //添加版本号
                stmt.executeUpdate(sql);
            }
        } catch( Exception e ) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 连接群对应的数据库
     * @param groupId 群号
     * @return 连接情况
     */
    public boolean connect(long groupId) {
        return connect("jdbc:sqlite:" + EntryLib.DATABASES_FOLDER + groupId + ".db");
    }

    /**
     * 连接群分组对应的数据库
     * @param subgroup 群分组
     * @return 连接情况
     * @see Subgroup
     */
    public boolean connect(Subgroup subgroup) {
        return connect("jdbc:sqlite:" + EntryLib.DATABASES_FOLDER + subgroup.getName() + ".db");
    }

    /**
     * 关闭数据库连接
     * @return 关闭状态
     */
    public boolean close() {
        try {
            stmt.close();
            c.close();
        } catch( Exception e ) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 查询表中存在的最大ID
     * 返回负数表示异常
     * @param table 表名
     * @return 最大ID，-1表示未连接数据库，-2表示异常，-3表示表为空
     */
    int max_id(String table) {
        if(c == null && stmt == null)return -1;

        try {
            String sql = "SELECT MAX(ID) as maxID from " + table + ";";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) return rs.getInt("maxID");
            else return -3;
        } catch( Exception e ) {
            e.printStackTrace();
            return -2;
        }
    }

    /**
     * 在主表中查询词条名所对应的词条ID
     * 确保 title 已经过单引号转义
     * 返回负数表示异常
     * @param title 词条名
     * @return 词条ID，-1表示未连接数据库，-2表示异常，-3表示未找到
     */
    public int find_id(String title) {
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
            e.printStackTrace();
            return -2;
        }
    }

    /**
     * 新建词条表，并在主表中添加索引
     * 确保 title 已经过单引号转义
     * 需保证即将创建的表不存在
     * @param title 词条名
     * @param type 匹配方式
     * @param priority 优先级
     * @return 创建状态
     */
    boolean create_map(String title, int type, int priority) {
        if(c == null && stmt == null) return false;

        try {
            int id = max_id("__MAIN_TABLE") + 1;

            String sql = "INSERT INTO __MAIN_TABLE (ID,TITLE,MATCH_MODE,PRIORITY) " +
                         "VALUES (" + id + ",'" + title + "'," + type + "," + priority + ");";
            stmt.executeUpdate(sql); //主表添加索引

            sql = "CREATE TABLE TABLE_" + id +
                  "(ID         INT            PRIMARY KEY NOT NULL," +                          //版本号
                  " CONTENT    nvarchar(1000) ," +                                              //内容
                  " TS         TIMESTAMP      NOT NULL DEFAULT (datetime('now','localtime')))"  //时间
            ;
            stmt.executeUpdate(sql); //创建新表

        } catch( Exception e ) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 查询 id 号词条表的匹配优先级
     * 返回负数表示异常
     * @param id 词条ID
     * @return 优先级
     */
    public int getPriority(int id) {
        if(c == null && stmt == null) return -1;

        String sql = "SELECT PRIORITY FROM __MAIN_TABLE WHERE ID=" + id + ";";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next())return rs.getInt("PRIORITY");
            else return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 向词条插入新内容
     * 保证已连接数据库
     * 返回错误信息
     * @param title 词条名
     * @param content 新内容
     * @param type 匹配方式
     * @param priority 优先级
     * @param ErrorInfo 传递错误信息
     * @return 插入状态
     */
    public boolean insert(String title, String content, int type, int priority, StringBuilder ErrorInfo) {
        if(c == null && stmt == null) return false;

        title = title.replace("'","''"); //单引号转义
        content = content.replace("'","''"); //单引号转义

        int id = find_id(title);
        if(id == -3)
            if(!create_map(title, type, priority)) {
                close();
                ErrorInfo.append("无法创建新表！");
                return false;
            } else id = max_id("__MAIN_TABLE");
        else if(id < 0) {
            close();
            ErrorInfo.append("数据库查询异常！");
            return false;
        } else {
            String sql = "UPDATE __MAIN_TABLE SET MATCH_MODE = " + type + ", PRIORITY = " + priority + " WHERE ID = " + id + ";"; //修改匹配方式与优先级
            try {
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                ErrorInfo.append("无法修改").append(id).append("号词条表的匹配方式与优先级！");
                e.printStackTrace();
                close();
                return false;
            }
        }

        String table = "TABLE_" + id;
        int tableId = max_id(table) + 1;

        try {
            String sql = "INSERT INTO " + table + " (ID,CONTENT)" +
                    "VALUES (" + tableId + ",'" + content +"');"
                    ;
            stmt.executeUpdate(sql);
        } catch( Exception e ) {
            ErrorInfo.append("无法向").append(table).append("词条表中插入数据！");
            e.printStackTrace();
            close();
            return false;
        }

        close();
        return true;
    }

    /**
     * 连接群数据库，向词条插入新内容
     * 返回错误信息
     * @param groupId 群号
     * @param title 词条名
     * @param content 新内容
     * @param type 匹配方式
     * @param priority 优先级
     * @param ErrorInfo 传递错误信息
     * @return 插入状态
     */
    public boolean insert(long groupId, String title, String content, int type, int priority, StringBuilder ErrorInfo) {
        if(!connect(groupId)) {
            ErrorInfo.append("数据库连接失败！");
            return false;
        }

        return insert(title, content, type, priority, ErrorInfo);
    }

    /**
     * 连接群分组数据库，向词条插入新内容
     * 返回错误信息
     * @param subgroup 群分组
     * @param title 词条名
     * @param content 新内容
     * @param type 匹配方式
     * @param priority 优先级
     * @param ErrorInfo 传递错误信息
     * @return 插入状态
     * @see Subgroup
     */
    public boolean insert(Subgroup subgroup, String title, String content, int type, int priority, StringBuilder ErrorInfo) {
        if(!connect(subgroup)) {
            ErrorInfo.append("数据库连接失败！");
            return false;
        }

        return insert(title, content, type, priority, ErrorInfo);
    }

    /**
     * 删除词条
     * 保证已连接数据库
     * 返回错误信息
     * @param title 词条名
     * @param ErrorInfo 传递错误信息
     * @return 删除状态
     */
    public boolean delete(String title, StringBuilder ErrorInfo) {
        if(c == null && stmt == null) return false;

        title = title.replace("'","''"); //单引号转义

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

        try {
            String sql = "DELETE FROM __MAIN_TABLE WHERE ID = " + id + ";";
            stmt.executeUpdate(sql);
        } catch( Exception e ) {
            ErrorInfo.append("无法删除主表行！");
            e.printStackTrace();
            close();
            return false;
        }

        String table = "TABLE_" + id;

        try {
            String sql = "DROP TABLE " + table + ";";
            stmt.executeUpdate(sql);
        } catch( Exception e ) {
            ErrorInfo.append("无法删除").append(table).append("词条表！");
            e.printStackTrace();
            close();
            return false;
        }

        close();
        return true;
    }

    /**
     * 连接群数据库，删除词条
     * 返回错误信息
     * @param groupId 群号
     * @param title 词条名
     * @param ErrorInfo 传递错误信息
     * @return 删除状态
     */
    public boolean delete(long groupId, String title, StringBuilder ErrorInfo) {
        if(!connect(groupId)) {
            ErrorInfo.append("数据库连接失败！");
            return false;
        }

        return delete(title, ErrorInfo);
    }

    /**
     * 连接群分组数据库，删除词条
     * 返回错误信息
     * @param subgroup 群分组
     * @param title 词条名
     * @param ErrorInfo 传递错误信息
     * @return 删除状态
     * @see Subgroup
     */
    public boolean delete(Subgroup subgroup, String title, StringBuilder ErrorInfo) {
        if(!connect(subgroup)) {
            ErrorInfo.append("数据库连接失败！");
            return false;
        }

        return delete(title, ErrorInfo);
    }

    /**
     * 查询词条的最新内容
     * 保证已连接数据库
     * 返回错误信息
     * @param id 词条id
     * @param random 是否随机版本回复
     * @param ErrorInfo 传递错误信息
     * @return 词条最新内容
     */
    public String query(int id, boolean random, StringBuilder ErrorInfo) {
        if(c == null && stmt == null) return null;

        String table = "TABLE_" + id;
        int tableId = max_id(table);

        if(random) {
            Random rd = new Random();
            tableId = rd.nextInt(tableId) + 1;
        }

        try {
            String sql = "SELECT * FROM " + table + " WHERE ID=" + tableId + ";";
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
            ErrorInfo.append("无法在").append(table).append("词条表中查询数据！");
            e.printStackTrace();
            close();
            return null;
        }
    }

    /**
     * 连接群数据库，查询词条的最新内容
     * 返回错误信息
     * @param groupId 群号
     * @param id 词条id
     * @param random 是否随机版本回复
     * @param ErrorInfo 传递错误信息
     * @return 词条最新内容
     */
    public String query(long groupId, int id, boolean random, StringBuilder ErrorInfo) {
        if(!connect(groupId)) {
            ErrorInfo.append("数据库连接失败！");
            return null;
        }

        return query(id, random, ErrorInfo);
    }

    /**
     * 连接群分组数据库，查询词条的最新内容
     * 返回错误信息
     * @param subgroup 群分组
     * @param id 词条id
     * @param random 是否随机版本回复
     * @param ErrorInfo 传递错误信息
     * @return 词条最新内容
     * @see Subgroup
     */
    public String query(Subgroup subgroup, int id, boolean random, StringBuilder ErrorInfo) {
        if(!connect(subgroup)) {
            ErrorInfo.append("数据库连接失败！");
            return null;
        }

        return query(id, random, ErrorInfo);
    }

    /**
     * 查询词条的历史内容
     * 保证已连接数据库
     * 返回错误信息
     * @param id 词条id
     * @param ErrorInfo 传递错误信息
     * @return 返回一个表，储存所有历史项，每项都是 QueryValue 类型
     * @see QueryValue
     */
    public List<QueryValue> history(int id, StringBuilder ErrorInfo) {
        if(c == null && stmt == null) return null;

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
            ErrorInfo.append("无法在").append(table).append("词条表中查询数据！");
            e.printStackTrace();
            close();
            return null;
        }

        close();
        return list;
    }

    /**
     * 连接群数据库，查询词条的历史内容
     * 返回错误信息
     * @param groupId 群号
     * @param id 词条id
     * @param ErrorInfo 传递错误信息
     * @return 返回一个表，储存所有历史项，每项都是 QueryValue 类型
     * @see QueryValue
     */
    public List<QueryValue> history(long groupId, int id, StringBuilder ErrorInfo) {
        if(!connect(groupId)) {
            ErrorInfo.append("数据库连接失败！");
            return null;
        }

        return history(id, ErrorInfo);
    }

    /**
     * 连接群分组数据库，查询词条的历史内容
     * 返回错误信息
     * @param subgroup 群分组
     * @param id 词条id
     * @param ErrorInfo 传递错误信息
     * @return 返回一个表，储存所有历史项，每项都是 QueryValue 类型
     * @see QueryValue
     * @see Subgroup
     */
    public List<QueryValue> history(Subgroup subgroup, int id, StringBuilder ErrorInfo) {
        if(!connect(subgroup)) {
            ErrorInfo.append("数据库连接失败！");
            return null;
        }

        return history(id, ErrorInfo);
    }
}
