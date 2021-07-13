package com.billyang.entrylib.Config;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * EnableGroups 类
 * 对群开关进行控制
 * @author Bill Yang
 */
public class EnableGroups {

    String path;
    UserIO uio;

    /**
     * 初始化群开关文件
     */
    void initFile() {
        File file = new File(path,"switch.json");
        if(!file.exists()) {
            try {
                file.createNewFile();
                FileOutputStream fop = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fop,"UTF-8");

                writer.append("{\n" +
                        "}"
                );

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
     * @param uio 提供用户配置
     */
    public void init(String path, UserIO uio) {
        this.path = path;
        this.uio = uio;

        initFile();

    }

    /**
     * 检查某群开关是否开启
     * 默认值根据全局配置决定
     * @param groupId 群号
     * @return 开关状态
     * @see UserIO#getDefaultSwitch()
     */
    public boolean check(long groupId) {
        File file = new File(path,"switch.json");
        if(!file.exists()) initFile();

        StringBuffer sb = UserIO.readFile(file);

        try { //如果表中存在记录，返回记录值
            JSONObject configJson = JSONObject.parseObject(sb.toString());

            String value = configJson.getString(String.valueOf(groupId));

            if(value != null) return Boolean.parseBoolean(value);
        } catch (Exception e) {
            //e.printStackTrace();
            //由于不匹配结果大量，取消异常反馈
        }

        return uio.getDefaultSwitch();
    }

    /**
     * 修改开关
     * @param groupId 群号
     * @param mode 目标值
     * @return 修改情况
     */
    boolean turn(long groupId, boolean mode) {
        File file = new File(path,"switch.json");
        if(!file.exists()) initFile();

        StringBuffer sb = UserIO.readFile(file);

        try {
            JSONObject configJson = JSONObject.parseObject(sb.toString());

            configJson.put(String.valueOf(groupId), mode);

            String config = configJson.toJSONString();

            FileOutputStream fop = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fop,"UTF-8");

            writer.append(config);

            writer.close();
            fop.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 开启某群开关
     * @param groupId 群号
     * @return 开启状态
     * @see #turn(long, boolean)
     */
    public boolean turnOn(long groupId) {
        return turn(groupId,true);
    }

    /**
     * 关闭某群开关
     * @param groupId 群号
     * @return 开启状态
     * @see #turn(long, boolean)
     */
    public boolean turnOff(long groupId) {
        return turn(groupId,false);
    }

}
