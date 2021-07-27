package com.billyang.entrylib.MiraiCodeParser;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AtParser 类
 * At 处理器
 * 实现对 At 进行转义与反转义
 * @author Bill Yang
 */
public class AtParser {

    /**
     * 将 At target 转化为 Mirai 码
     * @deprecated 已有可靠官方接口，弃用本方法
     * @param target At target
     * @return Mirai 码
     */
    public static String Id2MiraiCode(long target) {return "[mirai:at:" + target + "]";}

    /**
     * 将表情 Mirai 码转化为 At target
     * @param code Mirai 码
     * @return At target
     */
    public static long MiraiCode2Id(String code) {return Long.parseLong(code.replace("[mirai:at:", "").replace("]",""));}

    /**
     * 将消息队列中的 At 转义为作为 Mirai 码的字符串
     * @param msgChain 消息队列
     * @return 转义后的消息队列
     */
    public MessageChain At2PlainText(MessageChain msgChain) {
        MessageChainBuilder builder = new MessageChainBuilder();

        for(SingleMessage msg : msgChain) {
            if(msg instanceof At) {
                At at = (At) msg;

                msg = new PlainText(at.serializeToMiraiCode());
            }
            builder.append(msg);
        }

        return builder.build();
    }

    /**
     * At Mirai 码的正则匹配式
     * @see At
     */
    public static String regex = "\\[mirai:at:[0-9]*]";

    /**
     * 将纯文本中的 At Mirai 码反转义为 At
     * @param g 原消息事件
     * @param msg 纯文本
     * @return 反转义后的消息队列
     */
    public MessageChain PlainText2At(GroupMessageEvent g, String msg) {
        MessageChainBuilder builder = new MessageChainBuilder();

        Pattern pt = Pattern.compile(regex);
        Matcher mt = pt.matcher(msg);

        int start, end = 0, lastEnd = 0;

        while(mt.find()) {
            start = mt.start();
            end = mt.end();

            if(start >= 1) builder.append(new PlainText(msg.substring(lastEnd, start)));

            long target = MiraiCode2Id(msg.substring(start, end));

            At at = new At(target);
            builder.append(at);

            lastEnd = end;
        }

        builder.append(new PlainText(msg.substring(end)));

        return builder.build();
    }

    /**
     * 将消息队列中的 At Mirai 码反转义为 At
     * @param g 原消息事件
     * @param msgChain 消息队列
     * @return 反转义后的消息队列
     */
    public MessageChain PlainText2At(GroupMessageEvent g, MessageChain msgChain) {
        MessageChainBuilder builder = new MessageChainBuilder();

        for(SingleMessage message: msgChain) {
            if(message instanceof PlainText) builder.append(PlainText2At(g, message.contentToString()));
            else builder.append(message);
        }

        return builder.build();
    }
}
