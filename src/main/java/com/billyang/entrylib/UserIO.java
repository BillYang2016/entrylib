package com.billyang.entrylib;

import com.alibaba.fastjson.JSONObject;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;

import java.io.*;
import java.util.Map;

/**
 * UserIO 类
 * 用户交互类
 * 包含对配置文件等的读取、修改
 * @author Bill Yang
 */
public class UserIO {

    JavaPlugin jp;
    String path;

    /**
     * 加载文件
     * 根据默认资源文件对目标文件缺失项进行补充
     * @param fileName 文件名
     */
    void loadFile(String fileName) {
        File file = new File(path, fileName);
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

                writeFile(file, configJson.toJSONString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 加载输入配置
     */
    void initInput() {loadFile("input.json");}

    /**
     * 加载输出配置
     */
    void initOutput() {loadFile("output.json");}

    /**
     * 加载全局配置
     */
    void initGlobalConfig() {loadFile("global.json");}

    /**
     * 初始化
     * @param jp 传递主类提供资源信息
     * @param path 提供数据路径
     */
    void init(JavaPlugin jp, String path) {

        this.jp = jp;
        this.path = path;

        initInput();
        initOutput();
        initGlobalConfig();

    }

    /**
     * 获取全局配置项值
     * @param key 全局配置项键
     * @return 全局配置项值
     */
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

    /**
     * 修改全局配置项
     * 若干二级方法对其进行了细化
     * @param key 全局配置项键
     * @param value 全局配置项值
     */
    public void modifyGlobalConfig(String key, int value) {
        File file = new File(path,"global.json");
        if(!file.exists()) initGlobalConfig();

        StringBuffer sb = readFile(file);

        try {
            JSONObject configJson = JSONObject.parseObject(sb.toString());

            configJson.put(key, value);

            writeFile(file, configJson.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取查询模式
     * @return 查询模式
     * @see #getGlobalConfig(String)
     */
    public boolean getViewMode() {return getGlobalConfig("view-mode").equals("1");}

    /**
     * 获取默认开关
     * @return 默认开关
     * @see #getGlobalConfig(String)
     */
    public boolean getDefaultSwitch() {return getGlobalConfig("default-switch").equals("1");}

    /**
     * 获取开关权限
     * @return 开关权限
     * @see #getGlobalConfig(String)
     */
    public boolean getSwitchPermission() {return getGlobalConfig("switch-permission").equals("1");}

    /**
     * 获取学习命令权限
     * @return 学习命令权限
     * @see #getGlobalConfig(String)
     */
    public boolean getLearnPermission() {return getGlobalConfig("learn-permission").equals("1");}

    /**
     * 获取查看命令权限
     * @return 查看命令权限
     * @see #getGlobalConfig(String)
     */
    public boolean getViewPermission() {return getGlobalConfig("view-permission").equals("1");}

    /**
     * 获取历史命令权限
     * @return 历史命令权限
     * @see #getGlobalConfig(String)
     */
    public boolean getHistoryPermission() {return getGlobalConfig("history-permission").equals("1");}

    /**
     * 获取搜索命令权限
     * @return 搜索命令权限
     * @see #getGlobalConfig(String)
     */
    public boolean getSearchPermission() {return getGlobalConfig("search-permission").equals("1");}

    /**
     * 获取全部词条命令权限
     * @return 全部词条命令权限
     * @see #getGlobalConfig(String)
     */
    public boolean getAllPermission() {return getGlobalConfig("all-permission").equals("1");}

    /**
     * 获取删除命令权限
     * @return 删除命令权限
     * @see #getGlobalConfig(String)
     */
    public boolean getDeletePermission() {return getGlobalConfig("delete-permission").equals("1");}

    /**
     * 获取图片缓存方式
     * @return 图片缓存方式
     * @see #getGlobalConfig(String)
     */
    public boolean getDownloadMode() {return getGlobalConfig("download-image").equals("1");}

    /**
     * 获取历史命令最多回复项数
     * @return 历史命令最多回复项数
     * @see #getGlobalConfig(String)
     */
    public int getHistoryMaxHeight() {return Integer.parseInt(getGlobalConfig("history-max-height"));}

    /**
     * 获取搜索命令最多回复项数
     * @return 搜索命令最多回复项数
     * @see #getGlobalConfig(String)
     */
    public int getSearchMaxHeight() {return Integer.parseInt(getGlobalConfig("search-max-height"));}

    /**
     * 获取回复方式
     * @return 回复方式（0：普通，1：at，2：引用）
     * @see #getGlobalConfig(String)
     */
    public int getReplyMode() {return Integer.parseInt(getGlobalConfig("reply-mode"));}

    /**
     * 解析指令
     * 判断用户输入是否构成命令，并标准化
     * @param command 用户输入
     * @return 命令标准格式
     */
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

    /**
     * 将插件标准输出用户化
     * @param fType 信息第一类型
     * @param sType 信息第二类型
     * @param args 信息参数
     * @return 用户化输出
     */
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

    /**
     * @deprecated 将插件标准输出用户化，并构筑回复方式
     * @param g 原消息事件
     * @param fType 信息第一类型
     * @param sType 信息第二类型
     * @param args 信息参数
     * @return Message 类用户化输出
     * @see #formatString(String, String, String...)
     * @see Message
     */
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

    /**
     * 构筑消息回复方式
     * @param g 原消息事件
     * @param msgChain 消息链表
     * @return Message 类用户化输出
     * @see #formatString(String, String, String...)
     * @see Message
     */
    Message format(GroupMessageEvent g, MessageChain msgChain) {
        Message reply;
        int replyMode = getReplyMode();
        if(replyMode == 0) reply = msgChain;
        else if(replyMode == 1) reply = new At(g.getSender().getId()).plus(msgChain);
        else reply = new QuoteReply(g.getSource()).plus(msgChain);

        return reply;
    }

    /**
     * 读入一个文件
     * @param file 文件名
     * @return 文件内容
     */
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

    /**
     * 写出文件
     * @param file 文件名
     * @param json 文件内容
     */
    static void writeFile(File file, String json) {
        String config = JsonFormatter.format(json);

        try {
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
