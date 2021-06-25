package com.billyang.entrylib;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;

public class MatchLoader {
    Database db;
    void init(Database db) {
        this.db=db;
    }
    String match(String title) {
        return null;
    }
    String search(String keyword) {
        return null;
    }
}
