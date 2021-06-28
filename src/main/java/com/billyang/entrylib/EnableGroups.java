package com.billyang.entrylib;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class EnableGroups { //群开关

    String path;
    UserIO uio;

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

    void init(String path,UserIO uio) {
        this.path = path;
        this.uio = uio;

        initFile();

    }

    boolean check(long groupId) {
        File file = new File(path,"switch.json");
        if(!file.exists())initFile();

        StringBuffer sb = UserIO.readFile(file);

        try { //如果表中存在记录，返回记录值
            JSONObject configJson = JSONObject.parseObject(sb.toString());

            return configJson.getBooleanValue(String.valueOf(groupId));
        } catch (Exception e) {
            //e.printStackTrace();
            //由于不匹配结果大量，取消异常反馈
        }

        return uio.getDefaultSwitch();
    }

    boolean turn(long groupId,boolean mode) {
        File file = new File(path,"switch.json");
        if(!file.exists())initFile();

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

    boolean turnOn(long groupId) {
        return turn(groupId,true);
    }

    boolean turnOff(long groupId) {
        return turn(groupId,false);
    }

}
