package com.billyang.entrylib;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;

import java.util.HashMap;
import java.util.Map;


/*
在settings.gradle.kts里改生成的插件.jar名称
build.gradle.kts里改依赖库和插件版本
在主类下的JvmPluginDescription改插件名称，id和版本
用runmiraikt这个配置可以在ide里运行，不用复制到mcl或其他启动器
 */

public final class JavaPluginMain extends JavaPlugin {
    public static final JavaPluginMain INSTANCE = new JavaPluginMain();
    private JavaPluginMain() {
        super(new JvmPluginDescriptionBuilder("com.billyang.entrylib", "0.1.0")
                .info("Ask and replay plugin for Mirai-Console")
                .author("Bill Yang")
                .build());
    }

    private Map<String,String> commands = new HashMap<>();
    Database db = new Database();
    MatchLoader ml = new MatchLoader();

    @Override
    public void onEnable() {
        getLogger().info("词条插件已加载完成！");

        commands.put("学习","learn"); //学习类命令
        commands.put("查看","view"); //查看类命令
        commands.put("历史","history"); //历史类命令
        commands.put("搜索","search"); //搜索类命令

        db.init(); //初始化数据库
        ml.init(db); //初始化匹配器

        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, g -> {
            //监听群消息
            //getLogger().info(g.getMessage().contentToString());

            String msg=g.getMessage().contentToString();

            String[] splited_msg = msg.split("#");

            if(splited_msg.length < 2) return; //不构成命令格式

            String command = commands.get(splited_msg[0]);

            if(command == null) return; //无对应命令
            else if(command == "learn") { //学习类命令

                if(splited_msg.length < 3) { //命令格式错误
                    return;
                }
                String title = splited_msg[1]; //词条名
                String content = splited_msg[2]; //词条内容
                int type = 0; //匹配方式

                if(splited_msg.length > 3) {
                    String stype = splited_msg[3];
                    if(stype == "精准") type = 0;
                    else if(stype == "模糊") type = 1;
                    else if(stype == "正则") type = 2;
                }

                boolean status = db.insert(title,content,type); //向数据库插入

                if(status == true)g.getGroup().sendMessage("已更新" + title + "词条！");
                else g.getGroup().sendMessage("更新" + title + "词条失败！");

            } else if(command == "view") { //查看类命令

                String title = splited_msg[1]; //词条名
                title = ml.match(title); //标准化词条名

                if(title == null) { //未找到

                } else {
                    String content = db.query(title);
                    g.getGroup().sendMessage(title + "的内容如下：\n--------\n"+content);
                }

            } else if(command == "history") { //历史类命令

                String title = splited_msg[1]; //词条名
                title = ml.match(title); //标准化词条名

                if(title == null) { //未找到

                } else {
                    String content = db.history(title);
                    g.getGroup().sendMessage(title + "的历史情况如下：\n--------\n"+content);
                }

            } else if(command == "search") { //搜索类命令

                String keyword = splited_msg[1]; //关键词
                String reply = ml.search(keyword); //标准化词条名
                g.getGroup().sendMessage("搜索到如下词条：\n--------\n"+reply);

            }

        });
        GlobalEventChannel.INSTANCE.subscribeAlways(FriendMessageEvent.class, f -> {
            //监听好友消息
            //getLogger().info(f.getMessage().contentToString());
        });
    }
}