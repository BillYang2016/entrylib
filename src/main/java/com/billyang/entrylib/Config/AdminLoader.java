package com.billyang.entrylib.Config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * AdminLoader 类
 * 用于加载与查验管理员身份
 */
public class AdminLoader {

    String path;

    /**
     * 初始化管理员文件
     */
    void initFile() {
        File file = new File(path,"admin.json");
        if(!file.exists()) {
            try {
                file.createNewFile();
                FileOutputStream fop = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fop,"UTF-8");

                writer.append("[  ]");

                writer.close();
                fop.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 初始化
     * @param path 提供数据路径
     */
    public void init(String path) {
        this.path = path;

        initFile();
    }

    /**
     * 根据 QQ 账号检查用户是否是管理员
     * @param qqId 提供账号
     * @return 是否是管理员
     */
    public boolean isAdmin(long qqId) {
        File file = new File(path,"admin.json");
        if(!file.exists()) initFile();

        StringBuffer sb = UserIO.readFile(file);

        try {
            List<Long> adminArray = JSONArray.parseArray(sb.toString(), Long.class);

            return adminArray.contains(qqId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
