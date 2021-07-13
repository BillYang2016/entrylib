package com.billyang.entrylib;

import com.billyang.entrylib.Config.UserIO;

/**
 * Security 类
 * 实现安全校验
 * @author Bill Yang
 */
public class Security {
    /**
     * 检查词条名是否合法
     * @param uio 提供用户配置
     * @param title 词条名
     * @return 合法状态
     */
    static boolean checkTitle(UserIO uio, String title) {
        if(title.toUpperCase().equals("__MAIN_TABLE")) return false; //与主表名相同

        String command = uio.parse(title);

        if(command == null) return true; //不是指令
        if(command.contains("switch")) return false; //与开关指令冲突
        if(command.equals("all")) return false; //与搜索全部指令冲突

        return true;
    }
}
