package com.billyang.entrylib;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;

import java.util.Collections;
import java.util.List;


/*
在settings.gradle.kts里改生成的插件.jar名称
build.gradle.kts里改依赖库和插件版本
在主类下的JvmPluginDescription改插件名称，id和版本
用runmiraikt这个配置可以在ide里运行，不用复制到mcl或其他启动器
 */

public final class JavaPluginMain extends JavaPlugin {
    public static final JavaPluginMain INSTANCE = new JavaPluginMain();
    private JavaPluginMain() {
        super(new JvmPluginDescriptionBuilder("EntryLib", "0.1.5")
                .id("com.billyang.entrylib")
                .info("Ask and replay plugin for Mirai-Console")
                .author("Bill Yang")
                .build());
    }

    Database db = new Database();
    MatchLoader ml = new MatchLoader();
    UserIO uio = new UserIO();
    EnableGroups eg = new EnableGroups();

    void processLearn(GroupMessageEvent g,String title,String content,int type) {
        StringBuilder ErrorInfo = new StringBuilder();
        boolean status = db.insert(g.getGroup().getId(), title, content, type, ErrorInfo); //向数据库插入

        if(status)g.getGroup().sendMessage(uio.format(g,"learn", "done", title));
        else {
            g.getGroup().sendMessage(uio.format(g,"learn", "fail", title));
            getLogger().warning(String.valueOf(ErrorInfo));
        }
    }

    void processView(GroupMessageEvent g,String title,boolean cancelError) {
        MatchValue mv = ml.match(g.getGroup().getId(), title);
        int id = mv.id; //获取匹配到的词条id
        int type = mv.type; //获取匹配到的匹配方式

        if(id < 0) { //未找到
            if(!cancelError)g.getGroup().sendMessage(uio.format(g,"view", "exist", title));
        } else {
            StringBuilder ErrorInfo = new StringBuilder(); //错误信息
            String content = db.query(g.getGroup().getId(), id, ErrorInfo);

            if(content == null) {
                if(!cancelError) {
                    g.getGroup().sendMessage(uio.format(g,"view", "error", title));
                    getLogger().warning(String.valueOf(ErrorInfo));
                }
            } else {
                if(type != 2)g.getGroup().sendMessage(uio.format(g,"view", "reply", title, content));
                else { //处理正则替换内容
                    ErrorInfo = new StringBuilder();

                    RegularReplace rr = new RegularReplace(id, mv.title, title, content);
                    content = rr.replace(ErrorInfo); //正则替换

                    if(content != null)g.getGroup().sendMessage(uio.format(g,"view", "reply", title, content));
                    else if(!cancelError) {
                        g.getGroup().sendMessage(uio.format(g,"view", "error", title));
                        getLogger().warning(String.valueOf(ErrorInfo));
                    }
                }
            }
        }
    }

    void processHistory(GroupMessageEvent g,String title,int page) {
        MatchValue mv = ml.match(g.getGroup().getId(), title);
        int id = mv.id; //获取匹配到的词条id
        int type = mv.type; //获取匹配到的匹配方式

        if(id < 0) { //未找到
            g.getGroup().sendMessage(uio.format(g,"history", "exist", title));
        } else {
            StringBuilder ErrorInfo = new StringBuilder(); //错误信息
            List<QueryValue> contentList = db.history(g.getGroup().getId(), id, ErrorInfo); //获取列表

            if(contentList == null) {
                g.getGroup().sendMessage(uio.format(g,"history", "error", title));
                getLogger().warning(String.valueOf(ErrorInfo));
            } else {
                int length = contentList.size(), maxHeight = uio.getHistoryMaxHeight(), maxPage; //计算最大页码
                try {
                    maxPage = (int) Math.ceil(1.0 * length / maxHeight);
                } catch (ArithmeticException e) { //除以0
                    g.getGroup().sendMessage(uio.format(g, "history", "error", title));
                    e.printStackTrace();
                    return;
                }

                if(page > maxPage || page <= 0) { //页码超过范围
                    g.getGroup().sendMessage(uio.format(g,"history", "empty", title, String.valueOf(page), String.valueOf(maxPage)));
                    return;
                }

                StringBuilder reply = new StringBuilder();
                int i = 0, begin = (page - 1) * maxHeight, end = page * maxHeight; //计算页数对应的编号始末

                Collections.reverse(contentList); //翻转列表

                for(QueryValue qv : contentList) { //依次格式化单条
                    if(i < begin) { //跳转至页首
                        i ++;
                        continue;
                    }
                    if(i >= end) break; //越过页尾

                    int versionId = qv.id;
                    String content = qv.content;
                    String time = qv.time;

                    if(type == 2) { //处理正则替换内容
                        ErrorInfo = new StringBuilder();

                        RegularReplace rr = new RegularReplace(id, mv.title, title, content);
                        content = rr.replace(ErrorInfo); //正则替换

                        if(content == null) {
                            g.getGroup().sendMessage(uio.format(g, "history", "error", title));
                            getLogger().warning(String.valueOf(ErrorInfo));
                            i ++;
                            continue;
                        }
                    }

                    String single = uio.formatString(g,"history", "single", String.valueOf(versionId), content, time);
                    reply.append(single);

                    i ++;
                }
                if(!reply.toString().equals(""))g.getGroup().sendMessage(uio.format(g,"history", "reply", title, reply.toString(), String.valueOf(page), String.valueOf(maxPage)));
                else g.getGroup().sendMessage(uio.format(g,"history", "empty", title, String.valueOf(page)));
            }
        }
    }

    void processSearch(GroupMessageEvent g,String keyword,int page) {
        List<MatchValue> list = ml.search(g.getGroup().getId(), keyword);
        StringBuilder reply = new StringBuilder();

        if(list.isEmpty()) { //未找到结果
            g.getGroup().sendMessage(uio.format(g,"search", "exist", keyword));
            return;
        }

        int length = list.size(), maxHeight = uio.getSearchMaxHeight(), maxPage; //计算最大页码

        try {
            maxPage = (int) Math.ceil(1.0 * length / maxHeight);
        } catch (ArithmeticException e) { //除以0
            g.getGroup().sendMessage(uio.format(g, "search", "error", keyword));
            e.printStackTrace();
            return;
        }

        if(page > maxPage || page <= 0) { //页码超过范围
            g.getGroup().sendMessage(uio.format(g,"search", "empty", keyword, String.valueOf(page), String.valueOf(maxPage)));
            return;
        }

        int i = 0, begin = (page - 1) * maxHeight, end = page * maxHeight; //计算页数对应的编号始末

        for(MatchValue mv : list) { //依次格式化单条
            if(i < begin) { //跳转至页首
                i ++;
                continue;
            }
            if(i >= end) break; //越过页尾

            String title = mv.title;
            int type = mv.type;
            String single;

            if(type != 2) single = uio.formatString(g,"search", "single", title);
            else single = uio.formatString(g,"search", "single-regex", title);

            reply.append(single);

            i ++;
        }
        g.getGroup().sendMessage(uio.format(g,"search", "reply", keyword, reply.toString(), String.valueOf(page), String.valueOf(maxPage)));
    }

    @Override
    public void onEnable() {

        String DataFolderPath = getDataFolder().getAbsolutePath();
        getLogger().info("配置文件目录：" + DataFolderPath);

        if(!db.init(DataFolderPath)) { //初始化数据库
            getLogger().error("无法加载数据库，请检查数据库是否损坏？");
            getLogger().error("插件无法正常运行，将停止加载。");
            return;
        }
        ml.init(db); //初始化匹配器
        uio.init(DataFolderPath); //初始化用户交互
        eg.init(DataFolderPath,uio); //初始化群开关

        getLogger().info("词条插件已加载完成！");

        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, g -> { //监听群消息

            String command = uio.parse(g.getMessage().contentToString()); //全局指令解析

            if(command != null) {
                if(!uio.getSwitchPermission() || g.getSender().getPermission() != MemberPermission.MEMBER) { //权限判断
                    if(command.equals("switch-on")) {
                        if(eg.turnOn(g.getGroup().getId()))g.getGroup().sendMessage(uio.format(g,"switch", "on"));
                        else g.getGroup().sendMessage(uio.format(g,"switch", "error"));
                    } else if(command.equals("switch-off")) {
                        if(eg.turnOff(g.getGroup().getId()))g.getGroup().sendMessage(uio.format(g,"switch", "off"));
                        else g.getGroup().sendMessage(uio.format(g,"switch", "error"));
                    }
                    return;
                }
            }

            if(!eg.check(g.getGroup().getId()))return; //开关未开启，不执行反馈

            String msg = g.getMessage().contentToString();

            msg = msg.replace("\\\\","__ANTI_ESCAPE__");
            msg = msg.replace("\\#","__ESCAPE_CHAR__"); //转义
            String[] splitedMsg = msg.split("#");
            for(int i = 0; i < splitedMsg.length; i ++) {
                splitedMsg[i] = splitedMsg[i].replace("__ESCAPE_CHAR__","#"); //转义回来
                splitedMsg[i] = splitedMsg[i].replace("__ANTI_ESCAPE__","\\");
            }

            if(uio.getViewMode()) { //查询模式为1，忽略命令格式
                if(splitedMsg.length >= 2) { //检查是否构成其他格式
                    command = uio.parse(splitedMsg[0]);
                    if(command != null)getLogger().info("Got Input Command: " + command);
                    else {
                        processView(g, msg,true);
                        return;
                    }
                } else {
                    processView(g, msg,true);
                    return;
                }
            } else { //查询模式为0，根据Input模块分析命令

                if(splitedMsg.length < 2) return; //不构成命令格式

                command = uio.parse(splitedMsg[0]);
                if(command != null)getLogger().info("Got Input Command: " + command);
            }

            if(command == null) return; //无对应命令
            else if(command.equals("learn")) { //学习类命令

                if(splitedMsg.length < 3)return; //命令格式错误

                String title = splitedMsg[1]; //词条名
                String content = splitedMsg[2]; //词条内容

                int type = 0; //匹配方式

                if(splitedMsg.length > 3) {
                    String sType = splitedMsg[3];

                    if(sType.contains("精准")) type = 0;
                    else if(sType.contains("模糊")) type = 1;
                    else if(sType.contains("正则")) type = 2;
                }

                processLearn(g, title, content, type);

            } else if(command.equals("view")) { //查看类命令

                processView(g, splitedMsg[1],false);

            } else if(command.equals("history")) { //历史类命令

                int page = 1;

                if(splitedMsg.length > 2) {
                    try { //尝试转换为数字
                        splitedMsg[2] = splitedMsg[2].trim();
                        page = Integer.parseInt(splitedMsg[2]);
                    } catch (Exception e) {
                        page = 1; //转换失败
                    }
                }

                processHistory(g, splitedMsg[1], page);

            } else if(command.equals("search")) { //搜索类命令

                int page = 1;

                if(splitedMsg.length > 2) {
                    try { //尝试转换为数字
                        splitedMsg[2] = splitedMsg[2].trim();
                        page = Integer.parseInt(splitedMsg[2]);
                    } catch (Exception e) {
                        page = 1; //转换失败
                    }
                }

                processSearch(g, splitedMsg[1],page);

            }

        });

        GlobalEventChannel.INSTANCE.subscribeAlways(FriendMessageEvent.class, f -> { //监听好友消息
            //待开发
        });
    }
}