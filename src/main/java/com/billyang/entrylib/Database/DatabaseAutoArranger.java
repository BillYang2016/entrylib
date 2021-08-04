package com.billyang.entrylib.Database;

import com.billyang.entrylib.EntryLib;
import com.billyang.entrylib.MiraiCodeParser.ImageParser;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ImageCleaner 类
 * 多线程，继承自 Runnable 接口
 * 清理没有用过的图片
 */
class ImageCleaner implements Runnable {
    private Thread t;

    EntryLib entrylib;
    private List<File> fileList;

    /**
     * 构造函数
     * @param entrylib 传递主类
     * @param fileList 提供已存在列表
     */
    ImageCleaner(EntryLib entrylib, List<File> fileList) {
        this.entrylib = entrylib;
        this.fileList = fileList;
    }

    /**
     * 线程运行方法
     */
    public void run() {
        File imageFolder = new File("data/EntryLib/images/");
        if(imageFolder.exists()) {
            List<File> tempList = Arrays.asList(imageFolder.listFiles());
            List<File> imageFiles = new ArrayList(tempList);
            HashSet<File> hs1 = new HashSet(fileList), hs2 = new HashSet(imageFiles);

            hs2.removeAll(hs1);
            imageFiles.clear();
            imageFiles.addAll(hs2); //得到需要删除的图片文件

            for(File file: imageFiles) {
                System.gc(); //需要先垃圾回收才能删除
                file.getAbsoluteFile().delete(); //删除图片
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            entrylib.getLogger().info("数据库整理器已清理未使用的图片缓存：总计" + imageFiles.size() + "个图片");
        }
    }

    /**
     * 线程开始方法
     */
    public void start() {
        if(t == null) {
            t = new Thread(this);
            t.start();
        }
    }
}

/**
 * DatabaseAutoArranger 类
 * 自动数据库整理器
 * 继承自 TimerTask 类
 * 定期执行数据库整理任务
 */
public class DatabaseAutoArranger extends TimerTask {

    EntryLib entrylib;
    Connection c;
    Statement stmt;

    /**
     * 构造函数
     * @param entrylib 传递主类
     */
    public DatabaseAutoArranger(EntryLib entrylib) {
        this.entrylib = entrylib;
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
            return false;
        }
    }

    /**
     * 连接数据库
     * @param database 数据库名
     * @return 连接成功与否
     */
    boolean connect(String database) {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:data/EntryLib/databases/" + database);

            stmt = c.createStatement();
            if(!exists(stmt, "__MAIN_TABLE")) return false;
        } catch( Exception e ) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 关闭数据库
     */
    void close() {
        try {
            stmt.close();
            c.close();
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * 整理数据库
     * 共两项工作：
     * - 删除游离表（即主表不存在记录但存在于数据库中的词条表）
     * - 将词条 id 整理成连续正整数
     * @param fileName 数据库名
     * @return 整理状态
     */
    boolean rearrange(String fileName) {
        List<Integer> list = new ArrayList<>(); //获取词条id列表

        try {
            String sql = "SELECT * FROM __MAIN_TABLE;";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                int id = rs.getInt("ID");
                list.add(id);
            }
            rs.close();
        } catch( Exception e ) {
            e.printStackTrace();
        }

        Collections.sort(list);

        try {
            String sql = "SELECT name FROM sqlite_master where type='table' order by name;";
            ResultSet rs = stmt.executeQuery(sql);
            List<Integer> existTables = new ArrayList<>(); //存在的table列表

            while(rs.next()) {
                String name = rs.getString("name");
                if(!name.equals("__MAIN_TABLE") && !name.equals("__VERSION"))existTables.add(Integer.parseInt(name.replace("TABLE_", "")));
            }

            HashSet<Integer> hs1 = new HashSet(list), hs2 = new HashSet(existTables);

            hs2.removeAll(hs1);
            existTables.clear();
            existTables.addAll(hs2); //获取游离表

            for(int tableId: existTables) {
                String table = "TABLE_" + tableId;

                boolean success = false;

                for(int i = 1; i <= 5; i ++) { //删除游离表，尝试五次
                    try {
                        sql = "DROP TABLE " + table + ";";
                        stmt.executeUpdate(sql);
                        success = true;
                        break;
                    } catch (SQLException e) {
                        entrylib.getLogger().error("无法删除游离表表" + table + "，五秒后重试！（" + i + "/5）");
                        e.printStackTrace();

                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                }

                if(!success) {
                    entrylib.getLogger().error("无法完成" + fileName + "数据库整理工作，即将退出！");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int mex = 1;

        for(int id: list) {
            if(id == mex) { //与最小编号相同，无需整理
                mex ++;
                continue;
            }

            String newTable = "TABLE_" + mex, oldTable = "TABLE_" + id;
            boolean success = false;

            for(int i = 1; i <= 5; i ++) { //复制原表内容至新表，尝试五次
                try {
                    String sql = "CREATE TABLE " + newTable + " AS SELECT * FROM " + oldTable + ";";
                    stmt.executeUpdate(sql);
                    success = true;
                    break;
                } catch (SQLException e) {
                    entrylib.getLogger().error("无法创建新表" + newTable + "，五秒后重试！（" + i + "/5）");
                    e.printStackTrace();

                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }

            if(!success) {
                entrylib.getLogger().error("无法完成" + fileName + "数据库整理工作，即将退出！");
                return false;
            }
            success = false;

            for(int i = 1; i <= 5; i ++) { //修改主表索引，尝试五次
                try {
                    String sql = "UPDATE __MAIN_TABLE SET ID = " + mex + " WHERE ID = " + id + ";";
                    stmt.executeUpdate(sql);
                    success = true;
                    break;
                } catch (SQLException e) {
                    entrylib.getLogger().error("无法创建修改主表关于id" + id + "的索引，五秒后重试！（" + i + "/5）");
                    e.printStackTrace();

                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }

            if(!success) {
                entrylib.getLogger().error("无法完成" + fileName + "数据库整理工作，即将退出！");
                return false;
            }
            success = false;

            for(int i = 1; i <= 5; i ++) { //删除原表，尝试五次
                try {
                    String sql = "DROP TABLE " + oldTable + ";";
                    stmt.executeUpdate(sql);
                    success = true;
                    break;
                } catch (SQLException e) {
                    entrylib.getLogger().error("无法删除原表" + oldTable + "，五秒后重试！（" + i + "/5）");
                    e.printStackTrace();

                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }

            if(!success) {
                entrylib.getLogger().error("无法完成" + fileName + "数据库整理工作，即将退出！");
                return false;
            }

        }

        return true;
    }

    /**
     * 将字符串中的图片 Mirai 码提取出来
     * @param text 字符串
     * @return 图片文件列表
     */
    public List<File> Text2ImageFile(String text) {
        List<File> list = new ArrayList<>();

        Pattern pt = Pattern.compile(ImageParser.regex);
        Matcher mt = pt.matcher(text);

        int start, end = 0;

        while(mt.find()) {
            start = mt.start();
            end = mt.end();

            String imageId = ImageParser.MiraiCode2Id(text.substring(start, end));
            File file = new File("data/EntryLib/images/", imageId);

            if(file.exists())list.add(file);
        }

        return list;
    }

    List<File> fileList;

    void listImage(String fileName) {
        List<Integer> idList = new ArrayList<>(); //词条表编号列表

        try {
            String sql = "SELECT * FROM __MAIN_TABLE;";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                int id = rs.getInt("ID");
                String title = rs.getString("TITLE");

                idList.add(id);
                fileList.addAll(Text2ImageFile(title)); //添加标题
            }
            rs.close();
        } catch( Exception e ) {
            e.printStackTrace();
        }

        for(int id: idList) {
            try {
                String sql = "SELECT * FROM TABLE_" + id + ";";
                ResultSet rs = stmt.executeQuery(sql);
                while(rs.next()) {
                    String content = rs.getString("CONTENT");

                    fileList.addAll(Text2ImageFile(content)); //添加标题
                }
                rs.close();
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 定时任务
     * 整理词条库包含的所有数据库
     */
    @Override
    public void run() {
        File file = new File("data/EntryLib/databases/");
        if(!file.exists()) return;

        File[] files = file.listFiles();

        if(files == null) return;

        entrylib.getLogger().info("数据库整理器开始执行整理任务");

        fileList = new ArrayList<>();

        for(File dbFile: files) {
            if(dbFile.isDirectory()) continue;

            String fileName = dbFile.getName();
            if(!fileName.endsWith(".db")) continue; //保证文件是数据库文件

            entrylib.getLogger().info("开始整理数据库" + fileName);

            if(!connect(fileName)) {
                entrylib.getLogger().warning("无法整理数据库" + fileName + "：数据库连接失败！");
                continue;
            }

            if(rearrange(fileName))entrylib.getLogger().info("数据库" + fileName + "整理完成");

            listImage(fileName);

            close();
        }

        ImageCleaner imageCleaner = new ImageCleaner(entrylib, fileList);
        imageCleaner.start();

        entrylib.getLogger().info("数据库整理器已完成所有整理任务");
    }
}
