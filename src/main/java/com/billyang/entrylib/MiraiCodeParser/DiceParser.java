package com.billyang.entrylib.MiraiCodeParser;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DiceParser 类
 * 骰子处理器
 * 实现对骰子进行转义与反转义
 * @author Bill Yang
 */
public class DiceParser {

    /**
     * 将骰子转化为 Mirai 码
     * @deprecated 已有可靠官方接口，弃用本方法
     * @param value 骰子随机值
     * @return Mirai 码
     */
    public static String Id2MiraiCode(int value) {return "[mirai:dice:" + value + "]";}

    /**
     * 将骰子 Mirai 码转化为骰子
     * @param code Mirai 码
     * @return 骰子随机值
     */
    public static int MiraiCode2Id(String code) {return Integer.parseInt(code.replace("[mirai:dice:", "").replace("]",""));}

    /**
     * 将消息队列中的骰子转义为作为 Mirai 码的字符串
     * @param msgChain 消息队列
     * @return 转义后的消息队列
     */
    public MessageChain Dice2PlainText(MessageChain msgChain) {
        MessageChainBuilder builder = new MessageChainBuilder();

        for(SingleMessage msg : msgChain) {
            if(msg instanceof Dice) {
                Dice dice = (Dice) msg;

                msg = new PlainText(dice.serializeToMiraiCode());
            }
            builder.append(msg);
        }

        return builder.build();
    }

    /**
     * 骰子 Mirai 码的正则匹配式
     * @see Dice
     */
    public static String regex = "\\[mirai:dice:[0-6]]";

    /**
     * 将纯文本中的骰子 Mirai 码反转义为骰子
     * @param g 原消息事件
     * @param msg 纯文本
     * @return 反转义后的消息队列
     */
    public MessageChain PlainText2Dice(GroupMessageEvent g, String msg) {
        MessageChainBuilder builder = new MessageChainBuilder();

        Pattern pt = Pattern.compile(regex);
        Matcher mt = pt.matcher(msg);

        int start, end = 0, lastEnd = 0;

        while(mt.find()) {
            start = mt.start();
            end = mt.end();

            if(start >= 1) builder.append(new PlainText(msg.substring(lastEnd, start)));

            int value = MiraiCode2Id(msg.substring(start, end));

            Dice dice;
            if(value == 0) dice = Dice.random(); //如果数字为0，随机一下
            else dice = new Dice(value);
            builder.append(dice);

            lastEnd = end;
        }

        builder.append(new PlainText(msg.substring(end)));

        return builder.build();
    }

    /**
     * 将消息队列中的骰子 Mirai 码反转义为骰子
     * @param g 原消息事件
     * @param msgChain 消息队列
     * @return 反转义后的消息队列
     */
    public MessageChain PlainText2Dice(GroupMessageEvent g, MessageChain msgChain) {
        MessageChainBuilder builder = new MessageChainBuilder();

        for(SingleMessage message: msgChain) {
            if(message instanceof PlainText) builder.append(PlainText2Dice(g, message.contentToString()));
            else builder.append(message);
        }

        return builder.build();
    }
}
