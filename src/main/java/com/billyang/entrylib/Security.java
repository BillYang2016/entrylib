package com.billyang.entrylib;

public class Security {
   static boolean checkTitle(UserIO uio, String title) {
        if(title.toUpperCase().equals("__MAIN_TABLE")) return false; //与主表名相同

        String command = uio.parse(title);

        if(command == null) return true; //不是指令
        if(command.contains("switch")) return false; //与开关指令冲突
        if(command.equals("all")) return false; //与搜索全部指令冲突

        return true;
    }
}
