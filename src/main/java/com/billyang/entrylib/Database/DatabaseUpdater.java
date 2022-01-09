package com.billyang.entrylib.Database;

import com.billyang.entrylib.EntryLib;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DatabaseUpdater 类
 * 更新数据库格式
 */
public class DatabaseUpdater {

    EntryLib entrylib;
    Database database;

    static int VERSION = 1;

    /**
     * 构造函数
     * @param entrylib 传递主类
     */
    public DatabaseUpdater(EntryLib entrylib) {
        this.entrylib = entrylib;
        database = new Database();
    }

    /**
     * 获取数据库版本
     * 需要 database 连接到数据库
     * @return 数据库版本
     */
    int getVersion() {
        if(!database.exists(database.stmt, "__VERSION")) return 0;
        try {
            ResultSet rs = database.stmt.executeQuery("SELECT * FROM __VERSION;");
            if(rs.next()) return rs.getInt("VERSION");
            else return VERSION;
        } catch (SQLException e) {
            e.printStackTrace();
            return VERSION;
        }
    }

    /**
     * 查询数据库中某个表中某个列是否存在
     * @param table 表名
     * @param column 列名
     * @return 列的存在性
     */
    boolean exists(String table, String column) {
        String sql = "select sql from sqlite_master where tbl_name='" + table + "' and type='table';";
        try {
            ResultSet rs = database.stmt.executeQuery(sql);
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
     * 从数据库版本0升级到版本1
     */
    public boolean v0tov1() {
        if(!exists("__MAIN_TABLE", "PRIORITY")) {
            String sql = "ALTER TABLE __MAIN_TABLE ADD COLUMN PRIORITY INT NOT NULL DEFAULT 2000;"; //添加列
            try {
                database.stmt.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        if(!database.exists(database.stmt, "__VERSION")) {
            String sql = "CREATE TABLE __VERSION (VERSION         INT            PRIMARY KEY NOT NULL)"; //创造版本表
            try {
                database.stmt.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        String sql1 = "DELETE FROM __VERSION;"; //删除原有记录
        String sql2 = "INSERT INTO __VERSION (VERSION) VALUES (1);"; //添加版本号
        try {
            database.stmt.executeUpdate(sql1);
            database.stmt.executeUpdate(sql2);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 数据库升级任务
     */
    public void update() {
        File file = new File(Database.DATABASES_PATH);
        if(!file.exists()) return;

        File[] files = file.listFiles();

        if(files == null) return;

        entrylib.getLogger().info("开始升级数据库版本");

        for(File dbFile: files) {
            if(dbFile.isDirectory()) continue;

            String fileName = dbFile.getName();
            if(!fileName.endsWith(".db")) continue; //保证文件是数据库文件

            if(!database.connect("jdbc:sqlite:" + Database.DATABASES_PATH + fileName)) {
                entrylib.getLogger().warning("无法升级数据库" + fileName + "：数据库连接失败！");
                database.close();
                continue;
            }

            int currentVersion = getVersion();

            if(currentVersion == VERSION) {
                entrylib.getLogger().info("数据库" + fileName + "已为最新版本！");
                database.close();
                continue;
            }

            for(int v = currentVersion; v < VERSION; v++) { //依次执行升级
                try {
                    if(!((Boolean) this.getClass().getMethod("v" + v + "to" + "v" + (v + 1), new Class[]{}).invoke(this))) {
                        entrylib.getLogger().warning("数据库" + fileName + "：无法更新版本" + v + "to" + (v + 1) + "！");
                        break;
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    entrylib.getLogger().warning("数据库" + fileName + "：无法更新版本" + v + "to" + (v + 1) + "！");
                    e.printStackTrace();
                    break;
                }
            }

            entrylib.getLogger().info("数据库" + fileName + "已为成功升级至版本v" + VERSION + "！");

            database.close(); //关闭数据库
        }
    }

}
