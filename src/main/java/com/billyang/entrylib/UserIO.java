package com.billyang.entrylib;

import com.alibaba.fastjson.JSONObject;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.QuoteReply;

import java.io.*;

public class UserIO { //用户交互类

    String path;

    void initInput() {
        File file = new File(path,"input.json");
        if(!file.exists()) {
            try {
                file.createNewFile();
                FileOutputStream fop = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fop,"UTF-8");

                writer.append("{\n" +
                        "  \"打开词条开关\":\"switch-on\",\n" +
                        "  \"关闭词条开关\":\"switch-off\",\n" +
                        "  \"学习\":\"learn\",\n" +
                        "  \"查看\":\"view\",\n" +
                        "  \"历史\":\"history\",\n" +
                        "  \"搜索\":\"search\"\n" +
                        "}"
                );

                writer.close();
                fop.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    void initOutput() {
        File file = new File(path,"output.json");
        if(!file.exists()) {
            try {
                file.createNewFile();
                FileOutputStream fop = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fop,"UTF-8");

                writer.append("{\n" +
                        "  \"switch\":{\n" +
                        "    \"on\":\"已启用词条库插件！\",\n" +
                        "    \"off\":\"已关闭词条库插件！\",\n" +
                        "    \"error\":\"出错啦！\"\n" +
                        "  },\n" +
                        "  \"learn\":{\n" +
                        "    \"done\":\"已更新 $1 词条！\",\n" +
                        "    \"fail\":\"更新 $1 词条失败！\"\n" +
                        "  },\n" +
                        "  \"view\":{\n" +
                        "    \"reply\":\"$1 的内容如下：\\n--------\\n$2\",\n" +
                        "    \"exist\":\"未找到 $1 相关的词条！\",\n" +
                        "    \"error\":\"查询 $1 时出错啦！\"\n" +
                        "  },\n" +
                        "  \"history\":{\n" +
                        "    \"reply\":\"$1 的历史情况如下（第$3/$4页）：\\n--------\\n$2\",\n" +
                        "    \"single\":\"版本$1（修改时间：$3）：\\n$2\\n-----\\n\",\n" +
                        "    \"exist\":\"未找到 $1 相关的词条！\",\n" +
                        "    \"empty\":\"$1 词条的第$2页历史结果为空，总计$3页！\",\n" +
                        "    \"error\":\"查询 $1 时出错啦！\"\n" +
                        "  },\n" +
                        "  \"search\":{\n" +
                        "    \"reply\":\"根据 $1 搜索到如下词条：\\n$2\",\n" +
                        "    \"single\":\"- $1\",\n" +
                        "    \"exist\":\"未找到 $1 相关的词条！\",\n" +
                        "    \"error\":\"查询 $1 时出错啦！\"\n" +
                        "  }\n" +
                        "}"
                );

                writer.close();
                fop.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    void initGlobalConfig() {
        File file = new File(path,"global.json");
        if(!file.exists()) {
            try {
                file.createNewFile();
                FileOutputStream fop = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fop,"UTF-8");

                writer.append("{\n" +
                        "  \"view-mode\":0,\n" +
                        "  \"default-switch\":1,\n" +
                        "  \"switch-permission\":1,\n" +
                        "  \"history-max-height\":3,\n" +
                        "  \"reply-mode\":0\n" +
                        "}"
                );

                writer.close();
                fop.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    void init(String path) {

        this.path = path;

        initInput();

        initOutput();

        initGlobalConfig();

    }

    String getGlobalConfig(String key) {
        File file = new File(path,"global.json");
        if(!file.exists())initGlobalConfig();

        StringBuffer sb = readFile(file);

        try {
            JSONObject configJson = JSONObject.parseObject(sb.toString());

            return configJson.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    boolean getViewMode() {
        return getGlobalConfig("view-mode").equals("1");
    }

    boolean getDefaultSwitch() {
        return getGlobalConfig("default-switch").equals("1");
    }

    boolean getSwitchPermission() {
        return getGlobalConfig("switch-permission").equals("1");
    }

    int getHistoryMaxHeight() {
        return Integer.parseInt(getGlobalConfig("history-max-height"));
    }

    int getReplyMode() {
        return Integer.parseInt(getGlobalConfig("reply-mode"));
    }

    String parse(String command) {
        File file = new File(path,"input.json");
        if(!file.exists())initInput();

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

    String formatString(GroupMessageEvent g, String fType, String sType, String... args) {
        File file = new File(path,"output.json");
        if(!file.exists())initOutput();

        StringBuffer sb = readFile(file);

        try {
            JSONObject configJson = JSONObject.parseObject(sb.toString());
            JSONObject sJson = configJson.getJSONObject(fType);
            String answer = sJson.getString(sType);

            for(int i = 0; i < args.length; i++)answer = answer.replace("$" + (i + 1),args[i]);

            return answer;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    Message format(GroupMessageEvent g, String fType, String sType, String... args) {
        String answer = formatString(g, fType, sType, args);

        if(answer == null)return null;

        Message reply;
        int replyMode = getReplyMode();
        if(replyMode == 0)reply = new PlainText(answer);
        else if(replyMode == 1)reply = new At(g.getSender().getId()).plus(answer);
        else reply = new QuoteReply(g.getSource()).plus(answer);

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
