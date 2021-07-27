package com.billyang.entrylib.MiraiCodeParser;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AtAllParser 类
 * AtAll 处理器
 * 实现对 AtAll 进行转义与反转义
 * @author Bill Yang
 */
public class AtAllParser {

    /**
     * 将消息队列中的 AtAll 转义为作为 Mirai 码的字符串
     * @param msgChain 消息队列
     * @return 转义后的消息队列
     */
    public MessageChain AtAll2PlainText(MessageChain msgChain) {
        MessageChainBuilder builder = new MessageChainBuilder();

        for(SingleMessage msg : msgChain) {
            if(msg instanceof AtAll) {
                AtAll atAll = (AtAll) msg;

                msg = new PlainText(atAll.serializeToMiraiCode());
            }
            builder.append(msg);
        }

        return builder.build();
    }

    /**
     * AtAll Mirai 码的正则匹配式
     * @see AtAll
     */
    public static String regex = "\\[mirai:atall]";

    /**
     * 将纯文本中的 AtAll Mirai 码反转义为 AtAll
     * @param g 原消息事件
     * @param msg 纯文本
     * @return 反转义后的消息队列
     */
    public MessageChain PlainText2AtAll(GroupMessageEvent g, String msg) {
        MessageChainBuilder builder = new MessageChainBuilder();

        Pattern pt = Pattern.compile(regex);
        Matcher mt = pt.matcher(msg);

        int start, end = 0, lastEnd = 0;

        while(mt.find()) {
            start = mt.start();
            end = mt.end();

            if(start >= 1) builder.append(new PlainText(msg.substring(lastEnd, start)));

            builder.append(AtAll.INSTANCE);

            lastEnd = end;
        }

        builder.append(new PlainText(msg.substring(end)));

        return builder.build();
    }

    /**
     * 将消息队列中的 AtAll Mirai 码反转义为 AtAll
     * @param g 原消息事件
     * @param msgChain 消息队列
     * @return 反转义后的消息队列
     */
    public MessageChain PlainText2AtAll(GroupMessageEvent g, MessageChain msgChain) {
        MessageChainBuilder builder = new MessageChainBuilder();

        for(SingleMessage message: msgChain) {
            if(message instanceof PlainText) builder.append(PlainText2AtAll(g, message.contentToString()));
            else builder.append(message);
        }

        return builder.build();
    }

}
