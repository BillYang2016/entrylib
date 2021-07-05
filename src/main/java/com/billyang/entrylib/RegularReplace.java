package com.billyang.entrylib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegularReplace {

    String pattern, line, answer;
    int id;

    RegularReplace(int id, String pattern, String line, String answer) {
        this.id = id;
        this.pattern = pattern;
        this.line = line;
        this.answer = answer;
    }

    String replace(StringBuilder ErrorInfo) {

        Pattern pt = Pattern.compile(pattern);
        Matcher mt = pt.matcher(line);

        if(mt.find()) {
            int size = mt.groupCount();
            for(int i = 1; i <= size; i++)answer = answer.replace("$" + i, mt.group(i));
            return answer;
        } else {
            ErrorInfo.append("正则替换时失配！\n");
            return null;
        }

    }

}
