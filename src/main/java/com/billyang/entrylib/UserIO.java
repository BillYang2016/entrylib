package com.billyang.entrylib;

import com.alibaba.fastjson.JSONObject;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;

import java.io.*;
import java.util.Map;

public class UserIO { //用户交互类

    JavaPlugin jp;
    String path;

    void loadFile(String fileName) {
        File file = new File(path,fileName);
        if(!file.exists()) { //如果没有则复制
            try {
                file.createNewFile();
                FileOutputStream fop = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fop,"UTF-8");

                writer.append(jp.getResource(fileName));

                writer.close();
                fop.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else { //如果有则逐项检查，添加不存在的项
            StringBuffer sb = readFile(file);
            try {
                JSONObject configJson = JSONObject.parseObject(sb.toString());
                JSONObject templateJson = JSONObject.parseObject(jp.getResource(fileName));

                for(Map.Entry<String, Object> entry : templateJson.entrySet()) {
                    String firstKey = entry.getKey();
                    Object firstValue = entry.getValue();

                    if(firstValue instanceof String || firstValue instanceof Integer) { //单层
                        if(!configJson.containsKey(firstKey))configJson.put(firstKey,firstValue);
                    } else { //双层嵌套json
                        JSONObject sJson;
                        if(configJson.containsKey(firstKey)) {
                            sJson = configJson.getJSONObject(firstKey);
                        } else {
                            sJson = JSONObject.parseObject("{}");
                        }

                        for(Map.Entry<String, Object> entrySon : templateJson.getJSONObject(firstKey).entrySet()) {
                            String secondKey = entrySon.getKey();
                            Object secondValue = entrySon.getValue();
                            if(!sJson.containsKey(secondKey))sJson.put(secondKey, secondValue);
                        }

                        configJson.put(firstKey, sJson);
                    }
                }

                String config = JsonFormater.format(configJson.toJSONString());

                FileOutputStream fop = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fop,"UTF-8");

                writer.append(config);

                writer.close();
                fop.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void initInput() {loadFile("input.json");}

    void initOutput() {loadFile("output.json");}

    void initGlobalConfig() {loadFile("global.json");}

    void init(JavaPlugin jp, String path) {

        this.jp = jp;
        this.path = path;

        initInput();
        initOutput();
        initGlobalConfig();

    }

    String getGlobalConfig(String key) {
        File file = new File(path,"global.json");
        if(!file.exists()) initGlobalConfig();

        StringBuffer sb = readFile(file);

        try {
            JSONObject configJson = JSONObject.parseObject(sb.toString());

            return configJson.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    boolean getViewMode() {return getGlobalConfig("view-mode").equals("1");}

    boolean getDefaultSwitch() {return getGlobalConfig("default-switch").equals("1");}

    boolean getSwitchPermission() {return getGlobalConfig("switch-permission").equals("1");}

    boolean getLearnPermission() {return getGlobalConfig("learn-permission").equals("1");}

    boolean getViewPermission() {return getGlobalConfig("view-permission").equals("1");}

    boolean getHistoryPermission() {return getGlobalConfig("history-permission").equals("1");}

    boolean getSearchPermission() {return getGlobalConfig("search-permission").equals("1");}

    boolean getAllPermission() {return getGlobalConfig("all-permission").equals("1");}

    boolean getDeletePermission() {return getGlobalConfig("delete-permission").equals("1");}

    boolean getDownloadMode() {return getGlobalConfig("download-image").equals("1");}

    int getHistoryMaxHeight() {return Integer.parseInt(getGlobalConfig("history-max-height"));}

    int getSearchMaxHeight() {return Integer.parseInt(getGlobalConfig("search-max-height"));}

    int getReplyMode() {return Integer.parseInt(getGlobalConfig("reply-mode"));}

    String parse(String command) {
        File file = new File(path,"input.json");
        if(!file.exists()) initInput();

        StringBuffer sb = readFile(file);

        try {
            JSONObject configJson = JSONObject.parseObject(sb.toString());

            return configJson.getString(command);
        } catch (Exception e) {
            //e.printStackTrace();
            //由于不匹配结果大量，取消异常反馈
        }

        return null;
    }

    String formatString(String fType, String sType, String... args) {
        File file = new File(path,"output.json");
        if(!file.exists()) initOutput();

        StringBuffer sb = readFile(file);

        try {
            JSONObject configJson = JSONObject.parseObject(sb.toString());
            JSONObject sJson = configJson.getJSONObject(fType);
            String answer = sJson.getString(sType);

            for(int i = 0; i < args.length; i++)answer = answer.replace("$" + (i + 1), args[i]);

            return answer;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    Message format(GroupMessageEvent g, String fType, String sType, String... args) {
        String answer = formatString(fType, sType, args);

        if(answer == null) return null;

        Message reply;
        int replyMode = getReplyMode();
        if(replyMode == 0) reply = new PlainText(answer);
        else if(replyMode == 1) reply = new At(g.getSender().getId()).plus(answer);
        else reply = new QuoteReply(g.getSource()).plus(answer);

        return reply;
    }

    Message format(GroupMessageEvent g, MessageChain msgChain) {
        Message reply;
        int replyMode = getReplyMode();
        if(replyMode == 0) reply = msgChain;
        else if(replyMode == 1) reply = new At(g.getSender().getId()).plus(msgChain);
        else reply = new QuoteReply(g.getSource()).plus(msgChain);

        return reply;
    }

    static StringBuffer readFile(File file) {
        StringBuffer sb = null;
        try {
            FileInputStream fip = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fip, "UTF-8");
            sb = new StringBuffer();

            while (reader.ready())sb.append((char) reader.read());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb;
    }

}
