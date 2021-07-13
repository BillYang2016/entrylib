package com.billyang.entrylib.Matcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RegularReplace 类
 * 提供对正则匹配替换进行处理的方法
 * @author Bill Yang
 */
public class RegularReplace {

    String pattern, line, answer;
    int id;

    /**
     * 构造函数
     * @param id 词条id（未使用）
     * @param pattern 模式串（原词条名）
     * @param line 主串（匹配词条名）
     * @param answer 回复（词条内容）
     */
    public RegularReplace(int id, String pattern, String line, String answer) {
        this.id = id;
        this.pattern = pattern;
        this.line = line;
        this.answer = answer;
    }

    /**
     * 对本对象进行正则替换处理
     * @param ErrorInfo 传递错误信息
     * @return 返回替换完成的回复（词条内容）
     */
    public String replace(StringBuilder ErrorInfo) {

        Pattern pt = Pattern.compile(pattern);
        Matcher mt = pt.matcher(line);

        if(mt.find()) {
            int size = mt.groupCount();
            for(int i = 1; i <= size; i++) {
                String target = mt.group(i);
                if(target != null) answer = answer.replace("$" + i, target);
            }
            return answer;
        } else {
            ErrorInfo.append("正则替换时失配！\n");
            return null;
        }

    }

}
